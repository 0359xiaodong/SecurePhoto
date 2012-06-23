import webapp2
from google.appengine.ext import db
from google.appengine.api import users
import base64
from request_spr import ID_LENGTH
import json

class Hash_Submit(webapp2.RequestHandler):
    def get(self):     
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.out.write('Here you can submit the hash of your photo')
        
    def post(self):
        type = self.request.get('type')
        
        if type == 'SPI':
            self.submit_SPI()
        else:
            self.submit_SPR()
        
    def submit_SPI(self):
        record = Hash_Entry()
        
        record.user = users.get_current_user()
        record.device_id = self.request.get('device_id')
        record.spr_id = "" 
                
        record.image_id = self.request.get('image_id')
        record.image_hash = self.request.get('image_hash')
        
        record.put()
        self.response_OK()
    
    def submit_SPR(self):
        record = Hash_Entry()
        
        record.user = users.get_current_user()
        record.device_id = self.request.get('device_id')
                
        record.spr_id = self.request.get('spr_id')
        record.image_id = self.request.get('image_id')
        record.image_hash = self.request.get('image_hash')
        
        record.put()
        self.response_OK()
    
    def response_OK(self):
        response = {'status':1}
        self.response.out.write(json.dumps(response))
  
   
class Hash_Entry(db.Model):        
    time = db.DateTimeProperty(auto_now_add=True)
    user = db.UserProperty()
    device_id = db.StringProperty(default="0")
        
    spr_id = db.StringProperty()
    image_id = db.StringProperty()
    image_hash = db.StringProperty()
    
    def __str__(self):
        vals = (self.time, self.user, self.device_id, self.spr_id, self.image_id, self.image_hash)
         
        s = """Time: %s
        User: %s 
        Device: %s
        SPR ID: %s
        Frame ID: %s
        Frame Hash: %s""" % vals
                
        
        return s
        
        
              
    
    