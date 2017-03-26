import requests
import json
import re
import time

DEVELOPER_KEY = "AIzaSyCVo0bPtJ_x9AKxQK1xuhrnyTJfWlciyEw"
API_URL = "https://www.googleapis.com/youtube/v3/search?"
# part=id&maxResults=5&order=relevance&q=Dean+Martin+(I+Love+You)+For+Sentimental+Reasons&key={YOUR_API_KEY}

def cleanString(title):
	title = re.sub(r'[^\w\s]','',title)
	return title

def get_json_from_api(url):
	r = requests.get(url).json()
	return r

def youtube_search(query):
	time.sleep(0.5)
	part = "id"
	query = query.replace(" ", "+")
	url = API_URL + "part=" + part + "&order=relevance&maxResults=1" + "&q=" + query + "&key=" + DEVELOPER_KEY
	result = get_json_from_api(url)
	try:
		url = result.get("items")[0].get("id").get("videoId")
	except Exception as e:
		url = None
		print(e)
	return url

if __name__ == "__main__":
	# ID = "7Qp5vcuMIlk"
	# getComments(ID)
	pass