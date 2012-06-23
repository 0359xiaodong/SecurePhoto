import webapp2
from hash_submit import Hash_Entry
from time import mktime

class Hash_Retrieve(webapp2.RequestHandler):
    def get(self, id):
        q = Hash_Entry.all()
        q.filter("image_id =", id)
        
        results = q.fetch(1)
        self.response.headers['Content-Type'] = 'text/plain'
        if len(results) < 1:
            self.response.out.write("No entry found")
        else:
            record = results[0]
            
            self.response.headers['image_hash'] = str(record.image_hash)
            self.response.headers['time'] = str(long(mktime(record.time.timetuple())))
            self.response.headers['user'] = str(record.user)
            self.response.headers['device_id'] = str(record.device_id)
            self.response.headers['spr_id'] = str(record.spr_id) 
            self.response.out.write(str(results[0]))
        
             
                
       
   

        