import webapp2
from time import time
import json

class Get_Time(webapp2.RequestHandler):
    def get(self):
        epoch_time = int(time())
        
        response = {"time" : epoch_time}        
        self.response.headers['Content-Type'] = 'application/json'
        self.response.out.write(json.dumps(response))                
        