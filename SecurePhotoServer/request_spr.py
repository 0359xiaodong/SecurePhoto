import webapp2
import os
import base64
import json
from google.appengine.ext import db
from google.appengine.api import users

from datetime import datetime,timedelta

NO_EXPIRY = True
VALIDITY = timedelta(hours=6) 
ID_LENGTH = 20



class SPR_Provider(webapp2.RequestHandler):
    def get(self):
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.out.write('I will get you a SPRoll')
        
    def post(self):
        record = SPR_Record()        
        
        record.user = users.get_current_user()
        record.device_id = self.request.get('device_id')
        record.spr_id = base64.b64encode(os.urandom(ID_LENGTH)) 
        
        if NO_EXPIRY:         
            record.validity_date = None 
        else:
            record.validity_date = datetime.now() + VALIDITY

        record.put()
        
        response = { 'spr_id':record.spr_id,
                     'validity_date':record.validity_date,
                    }
        
        self.response.out.write(json.dumps(response))
                
                
       
     
        
class SPR_Record(db.Model):
    time = db.DateTimeProperty(auto_now_add=True)
    user = db.UserProperty(auto_current_user_add=True)
    device_id = db.StringProperty(default="0")
    spr_id = db.StringProperty(default="0")
    validity_date = db.DateTimeProperty()
    
    
    
    
        
        