import pandas as pd
from textblob import TextBlob
from collections import Counter
from nltk.corpus import stopwords
import random

stop = set(stopwords.words('english'))

def ExtractCertainArtists():
	artists = Counter()
	df = pd.read_csv("songdata.csv")
	target_artists = ["Eminem", "Dean Martin", "Pink Floyd", "Queen", "Ed Sheeran", "Linkin Park"]

	for thing in df.itertuples():
		try:
			if thing[1] in target_artists:
				artists[thing[1]]+=1
		except Exception as e:
			print(e)
	print(artists)

	df2 = df[df["artist"].isin(target_artists)]
	df2.to_csv("extractedArtists.csv", index=False)
	# print(df2.info())
	
	
	# ngrams = [" ".join(gram) for gram in blob.ngrams(n=7)]
	# print(getMatch("please tell me something", ngrams))
	# for ar in df["text"]:
	# 	blob = TextBlob(ar)
	# 	ngrams = [" ".join(gram) for gram in blob.ngrams(n=7)]
	# print(getMatch("you do not tell me what to do", processed_verses))
	# 	break
def preProcessSongs():
	pls = []
	df = pd.read_csv("extractedArtists.csv")
	for thing in df.itertuples():
		lyric = thing[4]
		lyrics = lyric.split("\n")
		random.shuffle(lyrics)
		processedLyrics = "\n".join(list(filter(lambda lyric: (10 <= len(TextBlob(lyric).words) <= 12), lyrics))[:5])
		pls.append(processedLyrics)
	df["text"] = pls
	df.to_csv("extractedArtists.csv", index=False)
		

if __name__ == '__main__':
	ExtractCertainArtists()
	preProcessSongs()