import mysql.connector
from mysql.connector import Error

def obtenerkey(ip):
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
            consulta = "SELECT Secreto FROM clientes_radius WHERE IP_Cliente = %s"
            cursor.execute(consulta, (ip,))
            
            # Obtener el resultado
            resultado = cursor.fetchone() 
            
            if resultado:
                valor = resultado[0]  
                return valor
            else:
                return None
                
    except Error as e:
        return None
        
    finally:
        # Cerrar la conexión siempre, incluso si hay errores
        if 'conexion' in locals() and conexion.is_connected():
            cursor.close()
            conexion.close()
            
def checker(direccion_ip):
    result= obtenerkey(direccion_ip)  
    return result                 