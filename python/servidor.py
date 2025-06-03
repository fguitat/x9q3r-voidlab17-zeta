import logging
import socket
import asyncio
import websockets
import json
import ssl
import time
import random
import sys
from pyrad.server import Server
from pyrad.packet import AccessRequest, AccessAccept, AccessChallenge, AccessReject, AccountingRequest, AccountingResponse
from pyrad.dictionary import Dictionary
from otp_validador import validador
from validador_secretos import checker
from buscador_clientes import usuarioendb
from descifrador import descifrar


##########ðESCOMENTAR SOLO PARA CAPTURAS WIRESHARK #####################################
'''''
if 'SSLKEYLOGFILE' not in os.environ:
    os.environ['SSLKEYLOGFILE'] = os.path.abspath('sslkeys.log')
'''''    
    
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("radius-server")

# Crear un contexto SSL personalizado para autenticación mutua
ssl_context = ssl.create_default_context(ssl.Purpose.SERVER_AUTH, cafile="/home/ferran/Documentos/radius/ca.crt")
ssl_context.minimum_version = ssl.TLSVersion.TLSv1_3
ssl_context.maximum_version = ssl.TLSVersion.TLSv1_3
ssl_context.load_cert_chain(certfile="/home/ferran/Documentos/radius/client.crt", keyfile="/home/ferran/Documentos/radius/client.key")

# Mantener una conexión persistente
websocket_connection = None
connection_lock = asyncio.Lock()

# Cache para evitar solicitudes duplicadas
recent_requests = {}

async def get_websocket_connection():
    global websocket_connection
    async with connection_lock:
        try:
            if websocket_connection is None:
                websocket_connection = await websockets.connect(
                    "wss://127.0.0.1:8443",
                    ssl=ssl_context,
                    ping_interval=20,
                    ping_timeout=10,
                    close_timeout=10
                )
            return websocket_connection
        except (ConnectionRefusedError, websockets.exceptions.InvalidHandshake) as e:
            logger.error(f"Error al conectar con WebSocket: {e}")
            websocket_connection = None
            raise
        except (ssl.SSLError) as ex:
            logger.error(f"Error al conectar con ssl {ex}")
            
async def verificar_acceso(username, request_id):
    try: 
        websocket = await get_websocket_connection()
        
        mensaje = {
            "tipo": "radius",
            "usuario": username,
            "request_id": request_id
        }
        await websocket.send(json.dumps(mensaje))
        logger.info(f"Solicitud enviada para {username} (request_id: {request_id})")
        
        try:
            respuesta = await asyncio.wait_for(websocket.recv(), timeout=30)
            # logger.info(f"Se recibe: {respuesta}")
            datos = json.loads(respuesta)
            
            # Verificar que la respuesta corresponde al request_id
            if datos.get("request_id") == request_id:
                if datos.get("respuesta") == "SI":
                    return True
                else:
                    return False
            else:
                return False
                
        except asyncio.TimeoutError:
            logger.error(f"Timeout esperando respuesta para {username} (request_id: {request_id})")
            return False
            
    except (websockets.exceptions.ConnectionClosed, ConnectionRefusedError) as e:
        logger.error(f"Error en la conexión WebSocket: {str(e)}")
        global websocket_connection
        websocket_connection = None
        return False
    except Exception as e:
        logger.error(f"Error inesperado en verificar_acceso: {str(e)}")
        return False
    finally:
        # No elimino el cache aquí para permitir que expire naturalmente
        pass

class RadiusServer(Server):
    def __init__(self, host="0.0.0.0", auth_port=1812, acct_port=1813):
        dictionary = Dictionary("/home/ferran/Documentos/radius/radius.dict")
        super().__init__(authport=auth_port, acctport=acct_port, dict=dictionary)
        
        # Socket para autenticación
        self.auth_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.auth_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.auth_socket.bind((host, auth_port))
        self.auth_socket.setblocking(False)
        
        # Socket para accounting
        self.acct_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.acct_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.acct_socket.bind((host, acct_port))
        self.acct_socket.setblocking(False)
        
        logger.info(f"RADIUS Server listening on {host}:{auth_port} (auth) and {host}:{acct_port} (acct)")

    async def HandleAuthPacket(self, pkt, variable):
        try:
            username = pkt["User-Name"][0]
            password = pkt.PwDecrypt(pkt["User-Password"][0])
            request_id = (int(time.time()) + random.randint(0, 999999)) % (2**31 - 1)
            reply = self.CreateReplyPacket(pkt)
            
            if "reg" in pkt:
                if usuarioendb(username, password):
                    reply.code = AccessReject if (resultado := descifrar(username, variable)) is None else AccessAccept
                    if reply.code == AccessAccept:
                        reply["Reply-Message"] = resultado
                else:
                    reply.code = AccessReject     

            elif "otp" in pkt:
                if validador(username, password, variable):
                    reply.code = AccessAccept
                else:
                    reply.code = AccessReject
            elif "preotp" in pkt:
                if usuarioendb(username, password):
                    reply.code = AccessChallenge
                else:
                    reply.code = AccessReject    
                        
            else: 
                if usuarioendb(username, password):
                    if await verificar_acceso(username, request_id): 
                        logger.info(f"Se recibe SÍ para {username} (request_id: {request_id})")
                        reply.code = AccessAccept
                    else:
                        logger.info(f"Se recibe NO para {username} (request_id: {request_id})")
                        reply.code = AccessReject
                else:
                    reply.code=AccessReject    
                    
            # Enviar respuesta
            self.auth_socket.sendto(reply.ReplyPacket(), pkt.source)

        except Exception as e:
            try:
                reply = self.CreateReplyPacket(pkt)
                reply.code = AccessReject
                reply["Reply-Message"] = f"Error en autenticación: {str(e)}"
                self.auth_socket.sendto(reply.ReplyPacket(), pkt.source)
                logger.info(f"Access-Reject enviado a {pkt.source} por error")
            except Exception as send_error:
                logger.error(f"Error al enviar Access-Reject: {send_error}")

    async def HandleAcctPacket(self, pkt):
        try:
            if pkt.code != AccountingRequest:
                logger.warning("Paquete recibido no es Accounting-Request")
                return

            username = pkt.get("User-Name", ["unknown"])[0]
            status_type = pkt.get("Acct-Status-Type", ["unknown"])[0]
            session_id = pkt.get("Acct-Session-Id", ["unknown"])[0]
            
            logger.info(f"Accounting para {username}: Status={status_type}, Session={session_id}")

            # Crear respuesta de accounting
            reply = self.CreateReplyPacket(pkt)
            reply.code = AccountingResponse
            self.acct_socket.sendto(reply.ReplyPacket(), pkt.source)
            
            logger.info(f"Accounting procesado para {username}")

        except Exception as e:
            logger.error(f"Error en accounting: {e}")

async def main(variable):   
    server = RadiusServer()
    try:
        logger.info("Servidor RADIUS iniciado")
        loop = asyncio.get_running_loop()

        async def handle_socket(sock, packet_type):
            while True:
                try:
                    data, addr = await loop.sock_recvfrom(sock, 4096)
                    client_ip = addr[0]
                    client_secret = checker(client_ip)

                    if not client_secret:
                        logger.warning(f"Secreto no encontrado para {client_ip}")
                        continue

                    if packet_type == "auth":
                        pkt = server.CreateAuthPacket(packet=data, secret=client_secret.encode("utf-8"))
                        pkt.source = addr
                        await server.HandleAuthPacket(pkt, variable)
                    else:
                        pkt = server.CreateAcctPacket(packet=data, secret=client_secret.encode("utf-8"))
                        pkt.source = addr
                        await server.HandleAcctPacket(pkt)
                except Exception as e:
                    logger.error(f"Error al procesar paquete en {packet_type}: {e}")

        # Crear tareas para manejar sockets de autenticación y accounting
        auth_task = loop.create_task(handle_socket(server.auth_socket, "auth"))
        acct_task = loop.create_task(handle_socket(server.acct_socket, "acct"))

        # Ejecutar ambas tareas concurrentemente
        await asyncio.gather(auth_task, acct_task)

    except KeyboardInterrupt:
        logger.info("Servidor detenido")
    finally:
        server.auth_socket.close()
        server.acct_socket.close()

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("No se recibió ninguna variable.")
        sys.exit(1)
    
    variable = sys.argv[1]  # recoge el primer argumento
    asyncio.run(main(variable))