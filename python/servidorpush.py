import asyncio
import websockets
import json
import random
import time
import ssl
import sys
from otp_validador import validador

# Variables globales
clientes_conectados = {}
respuestas_pendientes = {}

###############################SOLO DESCOMENTAR PARA WIRESHARK#########################################################
'''''
# --- SSLKEYLOGFILE para Wireshark ---
if 'SSLKEYLOGFILE' not in os.environ:
    os.environ['SSLKEYLOGFILE'] = os.path.abspath('sslkeys.log')
'''''

ssl_context = ssl.create_default_context(ssl.Purpose.CLIENT_AUTH)
ssl_context.minimum_version = ssl.TLSVersion.TLSv1_3
ssl_context.maximum_version = ssl.TLSVersion.TLSv1_3
ssl_context.load_cert_chain(certfile="/home/ferran/Documentos/radius/server.crt", keyfile="/home/ferran/Documentos/radius/server.key")
ssl_context.load_verify_locations(cafile="/home/ferran/Documentos/radius/ca.crt")
ssl_context.verify_mode = ssl.CERT_REQUIRED  # Obligatorio mTLS

async def timeout_respuesta(id_mensaje, request_id):
    try:
        await asyncio.sleep(25)
        if id_mensaje in respuestas_pendientes:
            print(f"[!] Timeout: No se recibió respuesta para id_mensaje {id_mensaje}")
            if "radius" in clientes_conectados:
                websocket_radius = clientes_conectados["radius"]
                try:
                    await websocket_radius.send(json.dumps({
                        "tipo": "resultado",
                        "respuesta": "NO",
                        "id_mensaje": id_mensaje,
                        "request_id": request_id
                    }))
                    print(f"[→] Respuesta 'NO' enviada al servidor Radius por timeout para id_mensaje {id_mensaje}, request_id {request_id}")
                except websockets.exceptions.ConnectionClosed:
                    print("[!] Error: Conexión con servidor Radius cerrada durante timeout")
            else:
                print("[!] Servidor Radius no conectado durante timeout")
            # Eliminar la solicitud pendiente
            del respuestas_pendientes[id_mensaje]
            print(f"[Cleanup] Solicitud pendiente {id_mensaje} eliminada tras timeout")
    except Exception as e:
        print(f"[!] Error en timeout_respuesta para id_mensaje {id_mensaje}: {str(e)}")

async def manejar_cliente(websocket, variable):
    usuario = None
    try:
        async for mensaje in websocket:
            try:
                mensaje_data = json.loads(mensaje)
                print("Mensaje recibido:", mensaje_data)

                # Clasificación de mensajes según su tipo
                if mensaje_data.get("tipo") == "radius":
                    # Manejo de mensaje de tipo radius
                    destino = mensaje_data.get("usuario")
                    request_id = mensaje_data.get("request_id")
                    id_mensaje = (int(time.time()) + random.randint(0, 999999)) % (2**31 - 1)
                    clientes_conectados["radius"] = websocket
                    if destino in clientes_conectados:
                        await clientes_conectados[destino].send(json.dumps({
                            "mensaje": "¿Autoriza el inicio de sesión?",
                            "id_mensaje": id_mensaje
                        }))
                        print(f"[→] Solicitud enviada a {destino} con id_mensaje {id_mensaje}, request_id {request_id}")
                        # Registrar solicitud pendiente
                        respuestas_pendientes[id_mensaje] = {
                            "destino": destino,
                            "websocket": websocket,
                            "timestamp": time.time(),
                            "request_id": request_id
                        }
                        print(f"[Pending] Solicitud pendiente registrada: {respuestas_pendientes}")
                        # Crear tarea de timeout
                        asyncio.create_task(timeout_respuesta(id_mensaje, request_id))
                    else:
                        print(f"[!] Destino {destino} no conectado")
                        await websocket.send(json.dumps({
                            "tipo": "resultado",
                            "respuesta": "NO",
                            "id_mensaje": id_mensaje,
                            "request_id": request_id
                        }))
                        print(f"[→] Respuesta 'NO' enviada al servidor Radius (destino no conectado) para id_mensaje {id_mensaje}, request_id {request_id}")

                elif mensaje_data.get("tipo") == "respuesta":
                    # Manejo de respuesta de autorización
                    respuesta = mensaje_data.get("respuesta")
                    id_mensaje = mensaje_data.get("id")
                    codigo = mensaje_data.get("code")
                    destino = respuestas_pendientes[id_mensaje]['destino']
                    if id_mensaje in respuestas_pendientes:
                        if validador(destino, codigo, variable):
                            respuesta = respuesta
                            print("Se está validando el OTP")
                        else: respuesta = "NO"    
                            
                        if "radius" in clientes_conectados:
                            try:
                                await clientes_conectados["radius"].send(json.dumps({
                                    "tipo": "resultado",
                                    "respuesta": respuesta,
                                    "id_mensaje": id_mensaje,
                                    "request_id": respuestas_pendientes[id_mensaje]["request_id"]
                                }))
                                print(f"[→] Respuesta reenviada al servidor Radius: {respuesta} para id_mensaje {id_mensaje}, request_id {respuestas_pendientes[id_mensaje]['request_id']}")
                            except websockets.exceptions.ConnectionClosed:
                                print("[!] Error: Conexión con servidor Radius cerrada al enviar respuesta")
                        else:
                            print("[!] Servidor Radius no conectado")            
                        # Eliminar la solicitud pendiente
                        del respuestas_pendientes[id_mensaje]
                        print(f"[Cleanup] Solicitud pendiente {id_mensaje} eliminada tras respuesta")
                    else:
                        print(f"[!] No se encontró solicitud pendiente para id_mensaje {id_mensaje}")

                else:
                    # Registro de cliente (no radius)
                    usuario = mensaje_data["usuario"]
                    clientes_conectados[usuario] = websocket
                    print(f"+ {usuario} conectado")

            except json.JSONDecodeError:
                print("[!] Error: Mensaje no válido (no es JSON)")
            except KeyError as e:
                print(f"[!] Error: Falta campo requerido en mensaje - {e}")

    except websockets.exceptions.ConnectionClosed as e:
        print(f"[!] Conexión cerrada para {usuario if usuario else 'cliente desconocido'}: {str(e)}")
    except Exception as e:
        print(f"[!] Error en conexión: {str(e)}")
    finally:
        # Limpieza al desconectar
        if usuario and usuario in clientes_conectados:
            del clientes_conectados[usuario]
            print(f"- {usuario} desconectado")
        elif websocket in clientes_conectados.values():
            # Remover radius si es el que se desconecta
            for key, value in list(clientes_conectados.items()):
                if value == websocket:
                    del clientes_conectados[key]
                    print("- Servidor Radius desconectado")
            # Limpiar solicitudes pendientes asociadas
            for id_mensaje in list(respuestas_pendientes.keys()):
                if respuestas_pendientes[id_mensaje]["websocket"] == websocket:
                    del respuestas_pendientes[id_mensaje]
                    print(f"[Cleanup] Solicitud pendiente {id_mensaje} eliminada tras desconexión de Radius")

async def main(variable):
    resultado = f"Procesando variable: {variable.upper()}"
    print(resultado)
    async def manejar_cliente_wrapper(websocket):
        await manejar_cliente(websocket, variable)
    # Crear servidor WebSocket
    server = await websockets.serve(
        manejar_cliente_wrapper,
        "0.0.0.0",
        8443,
        ssl=ssl_context,
        ping_interval=20,
        ping_timeout=10,
        close_timeout=10
    )
                              
    print("Servidor activo en wss://0.0.0.0:8443")
    await asyncio.Future()
    
if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("No se recibió ninguna variable.")
        sys.exit(1)
    
    variable = sys.argv[1]  # recoge el primer argumento
    asyncio.run(main(variable))
