import mysql.connector
from mysql.connector import Error
import bcrypt

def usuarioendb(username, passw):
    try:
        # Establecer la conexión
        conexion = mysql.connector.connect(
            host='localhost', 
            database='dbtfg',  
            user='ferran',         
            password='Fguiñat2025*'   
        )
        if conexion.is_connected():            
            # Crear un cursor para ejecutar consultas
            cursor = conexion.cursor()
            
            # Consulta con PreparedStatement
            consulta = "SELECT id, password FROM usuarios WHERE usuario = %s"
            cursor.execute(consulta, (username,))
            
            # Obtener el resultado
            resultado = cursor.fetchone() 
            
            if resultado:
                hashpass = resultado[1]
                if bcrypt.checkpw(passw.encode('utf-8'), hashpass.encode('utf-8')):
                    return True
                else:
                    return False
            else:
                return False
                
    except Error as e:
        return False
        
    finally:
        # Cerrar la conexión siempre, incluso si hay errores
        if 'conexion' in locals() and conexion.is_connected():
            cursor.close()
            conexion.close()
            