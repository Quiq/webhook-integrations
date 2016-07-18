from flask import Flask, request
import simplejson as json

app = Flask(__name__)

# TODO: Never compile your secret into your code... read this from a secure location or pass it in securely!
OUR_HIDDEN_SECRET = 'THE_SECRET_YOU_GOT_WHEN_YOU_REGISTERED'

@app.route('/handler/', methods=['POST'])
def handler():
    if request.headers.get('X-Centricient-Hook-Token') != OUR_HIDDEN_SECRET:
        print 'Warning: Someone besides Centricient is trying to call us!'
        return 'Invalid', 403

    event = request.get_json(force=True)
    print json.dumps(event, indent=2)

    if (event['eventType'] == 'test'):
        # This is called to test that the web-hook is available and working
        print 'Test was called'
    elif event['eventType'] == 'ConversationStatusChanged':
        # This is called for: new, active, inactive, closed
        print 'Conversation status changed to: ' + event['data']['status']

    return ''

if __name__ == '__main__':
    app.run()