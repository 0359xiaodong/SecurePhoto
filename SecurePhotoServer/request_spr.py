import webapp2
from google.appengine.ext import db
from google.appengine.api import users

from datetime import date

class SPR_Provider(webapp2.RequestHandler):
    def get(self):
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.out.write('I will get you a SPRoll')
        
    def post(self):
        record = SPR_Record()        
        
        record.user = users.get_current_user()
        record.device_id = self.request.get('device_id')
        record.spr_id = self.request.get('spr_id')
                
        val_date = self.request.get('validity_date')
        d = date.fromtimestamp(int(val_date))
        
        record.validity_date = d
        record.put()
                
                
       
     
        
class SPR_Record(db.Model):
    time = db.DateTimeProperty(auto_now_add=True)
    user = db.UserProperty(auto_current_user_add=True)
    device_id = db.StringProperty(default="0")
    spr_id = db.StringProperty(default="0")
    validity_date = db.DateTimeProperty()
    
    
    
    
        
        