import webapp2
from request_spr import SPR_Record
from time import mktime

class SPR_Retrieve(webapp2.RequestHandler):
    def get(self, id):
        q = SPR_Record.all()
        q.filter("spr_id =", id)
        
        results = q.fetch(1)
        self.response.headers['Content-Type'] = 'text/plain'
        if len(results) < 1:
            self.response.out.write("No entry found")                      
        else:           
            record = results[0]
            self.response.out.write(str(record))            
            self.response.headers['time'] = str(long(mktime(record.time.timetuple())))
            self.response.headers['user'] = str(record.user)
            self.response.headers['device_id'] = str(record.device_id)
            self.response.headers['spr_id'] = str(record.spr_id)        
        