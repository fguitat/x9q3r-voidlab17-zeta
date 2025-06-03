import base64
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
import mysql.connector

# Constantes equivalentes a las del código Java
AES_LENGTH_BIT = 256
IV_LENGTH_BYTE = 12  # Longitud típica de IV para GCM
TAG_LENGTH_BIT = 128  # Longitud típica del tag GCM (en bits)

def descifrar(user:str, clave:str) -> str:
    conexion=None
    cursor=None
    try:
        conexion = mysql.connector.connect(
            host='localhost',
            database='dbtfg',
            user='ferran',
            password='Fguiñat2025*'
        )
        
        cursor = conexion.cursor()
        consulta = "SELECT clave_secreta FROM usuarios WHERE usuario = %s"
        cursor.execute(consulta, (user,))
        resultado = cursor.fetchone()

        if not resultado:
            raise ValueError(f"No se encontró el usuario: {user}")

        texto_cifrado_b64 = resultado[0]  # texto cifrado en base64
        print("El texto cifrado es: "+ texto_cifrado_b64)

        # Decodificar desde Base64
        datos_cifrados = base64.b64decode(texto_cifrado_b64)

        # Separar IV, ciphertext y tag
        iv = datos_cifrados[:IV_LENGTH_BYTE]
        ciphertext_and_tag = datos_cifrados[IV_LENGTH_BYTE:]

        # AESGCM espera que el tag esté al final del ciphertext
        key_bytes = clave.encode('utf-8').ljust(AES_LENGTH_BIT // 8, b'\0')[:AES_LENGTH_BIT // 8]
        aesgcm = AESGCM(key_bytes)

        texto_plano = aesgcm.decrypt(iv, ciphertext_and_tag, None)
        
        if (consultarestado(user, conexion)):
            print("Desde descifrador se devuelve None")
            return None
        else:
            actualizar(user,conexion)
            return texto_plano.decode('utf-8')

    except Exception as e:
        print(f"Error al descifrar para el usuario {user}: {e}")
        return "ERROR"
    finally:
        if cursor:
            cursor.close()
        if consulta and conexion.is_connected():
            conexion.close()
            
def consultarestado(username:str, conexion) -> bool:
    try :
        cursor2=conexion.cursor()
        consulta = "SELECT estado from usuarios WHERE usuario = %s"
        cursor2.execute(consulta,(username,))        
        result=cursor2.fetchone()
        state=result[0]
        print("El estado vale: " + result[0])
        return state=="registrado"   
    except Exception as ex:
        return False
    finally:
        cursor2.close()
            
            
def actualizar(username, conexion):
    try:
        cursor3 = conexion.cursor()
        # Consulta con prepared statement
        consulta = "UPDATE usuarios SET estado = 'registrado' WHERE usuario = %s"
        cursor3.execute(consulta, (username,))
        
        # Confirmar los cambios
        conexion.commit()
            
    except mysql.connector.Error as e:
        raise Exception(f"Error de base de datos: {e}")
    finally:
        if cursor3:
            cursor3.close()               