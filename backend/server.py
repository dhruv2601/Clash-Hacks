from flask import Flask, jsonify, request
from pickSong import pick
from youtube_api import youtube_search

app = Flask(__name__)

@app.route("/getMatch", methods=["GET"])
def getMatch():
	sentense = request.args.get("sent")
	json = pick(sentense)
	for suggestion in json:
		try:
			suggestion["ytURL"] = youtube_search(suggestion["artist"] + " " + suggestion["song"])
		except Exception as e:
			print(e)
			suggestion["ytURL"] = None
	return jsonify(json)

@app.route("/")
def home():
	return "Hello World"

if __name__ == "__main__":
    app.run()
