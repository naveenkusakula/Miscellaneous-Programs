#!/usr/bin/python
import time;
import sys;
import re;
import socket;

# Define "constants" to be used throughout the program.

MY_VERSION = "3901chat/1.0";
ALL_OK = 200;
BAD_METHOD = 400;
BAD_AUTHENTICATION = 401;
BAD_ROOM = 402;
NOT_AUTHENTICATED = 403;

# The Chat class encapsulates all of the components of a chat
# protocol message

class Chat():
	# The chat system maintains the notion of a message  That message
	# contains a set of headers (key-value pairs), the payload of
	# the message (data), the command to execute (method), an object
	# for the action (the target), the version of the protocol
	# and a response code for return messages.

	def __init__ (self):
		self.headers = {}
		self.data = ''
		self.method = ''
		self.target = ''
		self.version = ''
		self.response = 0

	# Setter methods for each part of the message.

	def set_data( self, data ):
		self.data = data;

	def set_method( self, method ):
		self.method = method;

	def set_target( self, target ):
		self.target = target;

	def set_version( self, version ):
		self.version = version;

	def set_response( self, response ):
		self.response = response;

	def add_header( self, name, value ):
		self.headers[name] = value;

	# Getter methods for the message.

	def get_data( self ):
		return self.data;

	def get_method( self ):
		return self.method;

	def get_target( self ):
		return self.target;

	def get_version( self ):
		return self.version;

	def get_response( self ):
		return self.response;

	def get_header( self, name ):
		return self.headers[name];

	# Extract all the parts of a message from a string.
	# Parse the given message "message" and store the components
	# in the message structure of "self".

	def parse_message( self, message ):

		try:
			# Separate the headers from the body.
			# The two are dividied by a blank line,
			# recalling that all lines end with CRLF

			ind = message.index('\r\n\r\n');
			headers = message[:ind+2];
			self.data = message[ind+4:];

		except ValueError:
			headers = "";
			self.data = "";


		# Splitting on \r means that all lines except the first will
		# start with \n.  Account for that when storing their data

		lines = headers.split("\r");

		# The first line of the message must contain the method
		# requested of the chat server.

		elements = lines[0].split(" ");
		if len(elements) == 3:
			self.method = elements[0].lower();
			self.target = elements[1];
			self.version = elements[2].lower();
		else:
			self.method = "";
			self.target = "";
			self.version = "";
			

		# Run through the header lines and divide them into
		# key-value pairs by the first colon.

		i = 1;
		while (i < len(lines)) and (lines[i] != '\n'):
			elements = lines[i].split( ": " );
			self.headers[ elements[0][1:].lower() ] = elements[1];
			i += 1;

		# End the method with "self" having been loaded with
		# the message.



	# Create and return a string that is a formatted response 
	# based on the message data in "self"

	def gather_response( self ):
		# Initialize an array of the error messages for
		# well-known error conditions

		return_messages = {};
		return_messages[0] = "zero response";
		return_messages[ALL_OK] = "ok";
		return_messages[BAD_METHOD] = "Invalid operation";
		return_messages[BAD_AUTHENTICATION] = "Invalid authentication information provided";
		return_messages[BAD_ROOM] = "Invalid room requested";
		return_messages[NOT_AUTHENTICATED] = "Individual is not authenticated";

		# Initialize a blank message into which we will build 
		# the response.

		message = "";

		# Create the method line.  If it's using a message
		# return code for which we don't have a pre-defined
		# error message then return a generic 500-level
		# error code to show that there is an inconsistency
		# with the server itself.

		try:
			message = MY_VERSION + " " + str(self.response) + " " + return_messages[self.response] + "\r\n";
		except KeyError:
			message = MY_VERSION + " 500 Server error message table inconsistent.  Contact course instructor.\r\n";
		
		# Print out each relevant header line

		for index, key in enumerate( self.headers ):
			header = key;
			i = 0; 
			while i < len(header):
				if i==0:
					header = header[i].upper() + header[1:]
				elif header[i] == '-':
					header= header[:i+1] + header[i+1].upper() + header[i+2:]
				i += 1;
			message = message + header + ": " + str(self.headers[key]) + "\r\n";

		# End with the data, separated from the header with a blank line

		message = message + "\r\n" + self.data	

		return message;


	# Test whether all the requisite components of an incoming message
	# are present in a message.  Return True if all parts are present
	# and False otherwise.

	def is_valid(self):
		valid = False;

		if ( 
                        (((self.method == 'auth') and ('password' in self.headers)) or
                         ((self.method == 'say') and ('content-length' in self.headers) and (len(self.data) > 0) and ('cookie' in self.headers)) or
                         ((self.method == 'enter') and ('cookie' in self.headers)) or
                         ((self.method == 'exit') and ('cookie' in self.headers)) or
                         ((self.method == 'bye') and ('cookie' in self.headers)) or
                         ((self.method == 'noop') and ('cookie' in self.headers)) 
                         )
                        and (self.version == MY_VERSION.lower())  ):
				valid = True;

		return valid;


def debug_message( message, priority ):
	if debug_level >= priority:
		print message+"\n"



# Functions to manage each of therequest types that could come to the chat server.
# Each one returns a response message structure.


# Receive a parsed message "request" and verify whether or not the given 
# username and password match one of the entries of authorized users (input 
# parameter).  If recognized the create a cookie for the authorized user, 
# place it in a response message (response) and update the list of clients 
# with the new authorized user and the cookie.  That cookie will let the 
# user identify themselves to the server in the future.

def handle_authentication_message( request, authorized_users, clients, response ):

	debug_message( "Authentication message received", 1);

	# Extract the credentials from the message.
	
	user = request.get_target();
	password = request.get_header('password');

	# Check that we know the user and that the passwords match.

	if (user in authorized_users) and (password == authorized_users[ user ]):	
		# The user checks out.  Create a cookie value (poorly right now) 
		#for future interactions

		response.set_response( ALL_OK );
		cookie = "foo"+user;
		response.add_header( "Set-Cookie", cookie );

		# Remember this cookie as a client and remember the user for whom we create it.
		clients[ cookie ] = user;
	else:
		# Inconsistent information.  Deny the authentication request

		response.set_response( BAD_AUTHENTICATION );


# Receive a message (request) to enter a chat room.  Ensure that the room 
# is one that is supported, remove the individual from any existing room 
# of participation, and then place them into the new room.  Create a 
# response (response) to note the outcome.

def handle_enter_message(request, rooms, response):
	debug_message( "Enter message received", 1);

	# Find which room they want to enter and ensure that it's a valid room.

	new_room = request.get_target();
	if new_room in rooms:
		# Get out of current room
		for the_room in rooms:
			if the_cookie in rooms[ the_room ]:
				rooms[ the_room ].pop( the_cookie );

		# Enter new room
		rooms[ new_room ][ the_cookie ] = ""; 
		response.set_response( ALL_OK );
	else:
		# The room doesn't exist.
		debug_message( "Bad room choice", 2);
		response.set_response( BAD_ROOM );


# Receive a message (request) to leave the current chat room.  The user 
# leaves the room requested and is left in a state where they are in no 
# room, so cannot send any "say" messages until they enter a new room.  
# If the individual tries to leave a room in which they don't participate 
# then the protocol currently lets the user stay in whatever room they are 
# currently in and says that the individual is successfully not in the 
# requested room.


def handle_exit_message(request, rooms, response):
	debug_message( "Exit message received", 1);

	# Find the room that they want to exit and ensure that it actually exists
	the_room = request.get_target();
	if the_room in rooms:
		# Only exit the room if they're alread in it.  No matter what, the
		# individual will be out of that particular room, so indicate success
		# at leaving that room.
		if the_cookie in rooms[ the_room ]:
			rooms[ the_room ].pop( the_cookie );

		response.set_response( ALL_OK );
	else:
		# The room doesn't exist.
		debug_message( "Bad room given", 2);
		response.set_response( BAD_ROOM );


# Receive a message (request) to post information in one chat room.  After 
# ensuring that the request is ok (room ok and individual in the room), 
# the message is queued to be sent to all individuals in the room the next 
# time that they are sent a response from the system.  We complete a return 
# message (response) to let the poster know that the message is sent, part 
# of which includes returning the message to the poster.

def handle_say_message(request, rooms, response):
	debug_message( "Asking to speak in a room", 2);

	# Find the room that they think they're talking in.  Ensure that the room
	# exists and that the individual is in that room.

	the_room = request.get_target();

	if (the_room in rooms) and (the_cookie in rooms[ the_room ]):
		# Queue up the message to send to every other individual currently in the room
		for key in rooms[ the_room ]:
			rooms[the_room][key] += clients[the_cookie]+":"+request.get_data()+"\n";

		# Let the sender know that the room has heard their message.
		# Include any queued information (including the sender's message) 
		# in the response.

		response.set_response( ALL_OK );
		response.set_data( rooms[the_room][the_cookie] );
		response.add_header( "Content-Length", len( rooms[the_room][the_cookie] ));

		# Indicate that there is no more information queued to send to the 
		# sender

		rooms[the_room][the_cookie] = "" ; 
	else:
		# Either the room is wrong or the sender isn't in the room.  Deny the 
		# "say" request

		debug_message( "Bad room given", 2);
		response.set_response( BAD_ROOM );



# Process a message (request) to log out of the chat system.  We remove the
# individual from all rooms and from the list of clients who are in the chat
# system before completing a reply (response) to acknowledge the request.

def handle_bye_message(request, clients, rooms, response):
	debug_message( "Leaving the system", 2);

	# Remove the indvidual from any room to which they may currently belong

	for the_room in rooms:
		if the_cookie in rooms[ the_room ]:
			rooms[ the_room ].pop( the_cookie );

	# Invalidate the individuals' cookie so that the person is no longer
	# logged in
	clients.pop( the_cookie );

	response.set_response( ALL_OK );



# Process a no-operation message (request).  No work is done, but the server 
# has the chance to return any queued up information for the individual in 
# the reply back (response).

def handle_noop_message(request, rooms, response):
	debug_message( "Noop option.  Send back what I have.", 2);

	# Not much to do for the response.  Doing "nothing" really well.
	
	response.set_response( ALL_OK );

	# Search all the rooms to find any where the individual might be listening.
	# In sucha  room, return the queued messages fro the room back to the
	# individual as part of the response to the NOOP command.

	for the_room in rooms:
		if the_cookie in rooms[ the_room ]:
			response.set_response( ALL_OK );
			response.set_data( rooms[the_room][the_cookie] );
			response.add_header( "Content-Length", len( rooms[the_room][the_cookie] ));

			# Clear the queued messages
			rooms[the_room][the_cookie] = ""; 





# Begin the main program in earnest.  The chat server accepts
# non-persistent connections from clients to issue chat commands.
# The valid commands are
#
# auth -- authenticate a user for the chat system
# enter -- enter a chat room
# exit -- leave a chat room
# say -- issue a message in a chat room
# bye -- leave the chat system
# noop -- do nothing, but return any waiting information for the user.


# Define a place to store client registrations.

port = 20112;
debug_level = 0;  # Set to a higher level to get different degrees of debugging information printed.

# A hashmap to store all of the users who are registered with the chat
# server along with the cookie that is created for that user.

clients = {};

# For now, hard-code a list of users and passwords that can log in
# to the server.  Ideally, these should be in a file or a database
# instead.

authorized_users = {};
authorized_users[ "a" ] = "a";
authorized_users[ "b" ] = "b";
authorized_users[ "c" ] = "c";
authorized_users[ "d" ] = "d";
authorized_users[ "fred" ] = "flintstone";
authorized_users[ "betty" ] = "rubble";

# Only have a static set of chat rooms.  List them in the "rooms" hashmap.
# Each room is itself a hashmap that includes one entry for each client
# in the room and all of the text messages that the client hasn't yet
# seen from other clients in the room.

rooms = {};
rooms[ "3901" ] = {};
rooms[ "dal" ] = {};

close_client = True;

# Parse start-up information:
#   -p gives a new port number
#   -d sets a different debugging message level

i = 1;
while i < len(sys.argv):
	if sys.argv[i] == "-p":
		port = int(sys.argv[i+1]);
		i += 1;
	elif sys.argv[i] == "-d":
		debug_level = int(sys.argv[i+1]);
		i += 1;
	i+= 1;

# Proceed if we have a chance at a valid port for the connection

if port > 1024:
	print "Server using port " + str(port) + "\n";

	# Open the server socket and wait for some client to arrive.

	backlog = 1
	size = 1024
	serversock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	serversock.bind(('', port))
	serversock.listen(backlog)

	# Set the server into vigilant mode, waiting for clients

	while True:	
		# Accept a connection from a client and store
		# the socket for the new client

		clientsock, addr = serversock.accept()
	
		incoming = clientsock.recv(size)
		if not incoming:
			incoming = "";

		# Report the message that has arrived.

		if debug_level >= 3:
			debug_message( "incoming message", 3);
			debug_message( "------------------------------", 3);
			debug_message( incoming, 3);
			debug_message( "------------------------------", 3);

		# Prepare structures for the incoming message and the
		# ultiamte response.

		request = Chat();
		response = Chat();
		request.parse_message(incoming);

		if request.is_valid():
			# Invoke an appropriate function based on the method requested.

			if request.get_method() == 'auth':
				handle_authentication_message( request, authorized_users, clients, response );
			else:
				the_cookie = request.get_header( 'cookie' );
				if the_cookie in clients:
					if request.get_method() == 'enter':
						handle_enter_message(request, rooms, response);
					elif request.get_method() == 'exit':
						handle_exit_message(request, rooms, response);
					elif request.get_method() == 'say':
						handle_say_message(request, rooms, response);
					elif request.get_method() == 'bye':
						handle_bye_message(request, clients, rooms, response);
						close_client = True;
					elif request.get_method() == 'noop':
						handle_noop_message(request, rooms, response);
					else:
						debug_message( "Invalid method requested", 1);
						request.set_response( BAD_METHOD );
				else:
					debug_message( "Bad cookie provided", 2);
					response.set_response( NOT_AUTHENTICATED );

		else:
			debug_message( "Message structure missing elements", 1);
			response.set_response( BAD_METHOD );

		# Report back on what we're returning to the client if the
		# debugging level is high enough.
	
		if debug_level >= 3:
			msg = response.gather_response()
			if msg != "" :
				debug_message( "outgoing message", 3);
				debug_message( "------------------------------", 3);
				debug_message( msg, 3);
				debug_message( "------------------------------", 3);

		# Now send the response to the client.
	
		msg = response.gather_response();
		clientsock.send( msg );

		# We are only handling non-persistent connections for now.
		# Close the connection to the client.  They can call us back
		# with another request later.

		if close_client:
			clientsock.close();

	# Will never get here out of the infinite "while" loop.  However,
	# for propriety, recognize that we should close the server socket
	# if we ever do reach this point.

	serversock.close();

else:
	print "Invalid port provided\n";

