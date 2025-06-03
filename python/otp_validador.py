from mysql.connector import Error
import pyotp
from descifradorGCM import descifrar

def obtenerkey(usuario, variable):
    try:
        valor = descifrar(usuario, variable)  
        return valor            
    except Error as e:
        return None
        

def validador(user, codigo, variable):
    secreto= obtenerkey(user, variable)  
    totp = pyotp.TOTP(secreto, interval=30)

    if totp.verify(codigo, valid_window=1):
        return True
    else:
        return False
   
     