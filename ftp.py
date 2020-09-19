from pyftpdlib import servers
from pyftpdlib.handlers import FTPHandler
from pyftpdlib.authorizers import DummyAuthorizer
import os
address = ("0.0.0.0", 21)  # listen on every IP on my machine on port 21

# add user authorization
authorizer = DummyAuthorizer()

# Define a new user having full r/w permissions.
authorizer.add_user("user", "password", os.path.dirname(os.path.realpath(__file__)) + "\\sandbox\\ftp", perm='elradfmw')

FTPHandler.authorizer = authorizer

server = servers.FTPServer(address, FTPHandler)
server.serve_forever()