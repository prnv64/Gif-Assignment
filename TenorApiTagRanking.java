import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

class Gif {
    String id;
    String url;
    List<String> tags;

    public Gif(String id, String url, List<String> tags) {
        this.id = id;
        this.url = url;
        this.tags = tags;
    }

    public double getRelevanceScore(String searchTerm) {
        if (!tags.isEmpty() && tags.get(0).equalsIgnoreCase(searchTerm)) {
            return 2.0; // Higher score if the search term is the first tag
        }
        return tags.contains(searchTerm) ? 1.0 : 0.0; // Regular score if the search term is present but not the first tag
    }

    @Override
    public String toString() {
        return "Gif{id='" + id + "', url='" + url + "', relevanceScore=" + getRelevanceScore("download1") + "}";
    }
}

public class TenorApiTagRanking {

    public static void main(String[] args) {
        try {
            String apiKey = "AIzaSyCP-EBhb5jqbMuq1UqdPhPc_imEYFQ_hFg"; // Replace with your Tenor API key
            String query = "download1"; // Search term
            String urlString = "https://tenor.googleapis.com/v2/search?q=" + query + "&key=" + apiKey + "&limit=10";

            // Fetch GIF metadata
            @SuppressWarnings("deprecation")
			URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            conn.disconnect();

            // Parse JSON response
            JSONObject jsonResponse = new JSONObject(content.toString());
            JSONArray results = jsonResponse.getJSONArray("results");

            List<Gif> gifs = new ArrayList<>();

            for (int i = 0; i < results.length(); i++) {
                JSONObject gifObject = results.getJSONObject(i);
                String id = gifObject.getString("id");
                
                // Access the appropriate media format URL
                JSONObject mediaFormats = gifObject.getJSONObject("media_formats");
                String gifUrl = mediaFormats.getJSONObject("gif").getString("url");
                
                List<String> tags = new ArrayList<>();
                JSONArray tagsArray = gifObject.optJSONArray("tags");
                if (tagsArray != null) {
                    for (int j = 0; j < tagsArray.length(); j++) {
                        tags.add(tagsArray.getString(j));
                    }
                }

                gifs.add(new Gif(id, gifUrl, tags));
            }

            // Rank the GIFs based on tag relevance with priority as a first tag
            gifs.sort(Comparator.comparingDouble(gif -> ((Gif) gif).getRelevanceScore("download1")).reversed());

            // Print the top-ranked GIF
            if (!gifs.isEmpty()) {
                System.out.println("Top-ranked GIF:");
                System.out.println(gifs.get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
