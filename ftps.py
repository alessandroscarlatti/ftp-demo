"""
An RFC-4217 asynchronous FTPS server supporting both SSL and TLS.
Requires PyOpenSSL module (http://pypi.python.org/pypi/pyOpenSSL).
"""

from pyftpdlib.servers import FTPServer
from pyftpdlib.handlers import TLS_FTPHandler
from pyftpdlib.authorizers import DummyAuthorizer
import os
address = ("0.0.0.0", 21)  # listen on every IP on my machine on port 21

# add user authorization
authorizer = DummyAuthorizer()

# Define a new user having full r/w permissions.
authorizer.add_user("user", "password", os.path.dirname(os.path.realpath(__file__)) + "\\sandbox\\ftp", perm='elradfmw')


handler = TLS_FTPHandler
handler.authorizer = authorizer
certfile = os.path.dirname(os.path.realpath(__file__)) + "\\server.pem"
print(certfile)
handler.certfile = certfile
handler.tls_control_required = True
handler.tls_data_required = True

server = FTPServer(address, handler)
server.serve_forever()