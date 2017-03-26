import pandas as pd
from similarity import symmetric_sentence_similarity
import random

def getMatch(sentence, things):
	best = -10000000
	bestMatch = ""
	for gram in things:
		try:
			matchScore = symmetric_sentence_similarity(sentence, gram)
			if matchScore > best:
				best = matchScore
				bestMatch = gram
		except Exception as e:
			pass
	return (bestMatch, best)


def pick(sentense):
	bestMatchScore = -1000000
	bestMatch = []

	df = pd.read_csv("extractedArtists.csv")
	df = df.sample(frac=1)
	for song in df.itertuples():
		if len(bestMatch) > 15:
			random.shuffle(bestMatch)
			break
		try:
			lyrics = song[4].split("\n")
			song_name = song[2]
			artist_name = song[1]
			(cur_match,cur_best) = getMatch(sentense, lyrics)
			dict_obj_match = {"song":song_name, "artist":artist_name,"lyric_match":cur_match}
			if cur_best > bestMatchScore:
				bestMatch = [dict_obj_match]
				bestMatchScore = cur_best
			elif cur_best == bestMatchScore:
				bestMatch.append(dict_obj_match)
		except Exception as e:
			pass
	return(bestMatch[:3])




if __name__ == '__main__':
	print(pick("Do your own work do not disturb"))