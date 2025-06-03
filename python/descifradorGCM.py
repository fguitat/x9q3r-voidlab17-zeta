from cryptography.hazmat.primitives.ciphers.aead import AESGCM
import mysql.connector
import base64

def obtener_datos_cifrados_desde_db(username):
    conn = mysql.connector.connect(
        host="localhost",
        user="ferran",
        password="Fguiñat2025*",
        database="dbtfg"
    )
    cursor = conn.cursor()
    consulta = "SELECT clave_secreta FROM usuarios WHERE usuario = %s"
    cursor.execute(consulta, (username,))
    result = cursor.fetchone()
    cursor.close()
    conn.close()

    if result:
        try:
            return base64.b64decode(result[0])  # Decodifico Base64
        except Exception as e:
            return None
    return None


def descifrar(usuario, clave_str):
    datos_cifrados = obtener_datos_cifrados_desde_db(usuario)
    clave = clave_str.encode("utf-8")
    if len(clave) < 32:
        clave += b"\x00" * (32 - len(clave)) #Se iguala clave a 32 bytes como java
    elif len(clave) > 32:
        clave = clave[:32]

    iv = datos_cifrados[:12]
    ciphertext_con_tag = datos_cifrados[12:]  # Lo que queda es ciphertext+tag

    aesgcm = AESGCM(clave)
    try:
        texto_descifrado = aesgcm.decrypt(iv, ciphertext_con_tag, None)
        return texto_descifrado.decode("utf-8")
    except Exception as e:
        return None
