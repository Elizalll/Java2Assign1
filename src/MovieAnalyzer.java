import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Movie {
    String Series_Title; // Name of the movie
    int Released_Year; //Year at which that movie released
    String Certificate; //Certificate earned by that movie
    int Runtime; //Total runtime of the movie
    List<String> Genre; //Genre of the movie
    float IMDB_Rating; //Rating of the movie at IMDB site
    String Overview; //mini story/ summary
    int Meta_score; //Score earned by the movie
    String Director; //Name of the Director
    String Star1, Star2, Star3, Star4; //Name of the Stars
    List<String> starList;
    long Noofvotes; //Total number of votes
    long Gross; //Money earned by that movie

    Movie(String Series_Title, int Released_Year, String Certificate, int Runtime,
          List<String> Genre, float IMDB_Rating, String Overview, int Meta_score, String Director,
          String Star1, String Star2, String Star3, String Star4, long Noofvotes, long Gross) {
        this.Series_Title = Series_Title;
        this.Released_Year = Released_Year;
        this.Certificate = Certificate;
        this.Runtime = Runtime;
        this.Genre = Genre;
        this.IMDB_Rating = IMDB_Rating;
        this.Overview = Overview;
        this.Meta_score = Meta_score;
        this.Director = Director;
        this.Star1 = Star1;
        this.Star2 = Star2;
        this.Star3 = Star3;
        this.Star4 = Star4;
        this.starList = Arrays.asList(Star1, Star2, Star3, Star4);
        Collections.sort(this.starList);
        this.Noofvotes = Noofvotes;
        this.Gross = Gross;
    }

    public int getMeta_score() {
        return Meta_score;
    }

    public String getSeries_Title() {
        return Series_Title;
    }

    public String getOverview() {
        return Overview;
    }

    public String getStar4() {
        return Star4;
    }

    public String getStar3() {
        return Star3;
    }

    public String getStar2() {
        return Star2;
    }

    public List<String> getStarList() {
        return starList;
    }

    public String getStar1() {
        return Star1;
    }

    public String getDirector() {
        return Director;
    }

    public String getCertificate() {
        return Certificate;
    }

    public long getNoofvotes() {
        return Noofvotes;
    }

    public long getGross() {
        return Gross;
    }

    public List<String> getGenre() {
        return Genre;
    }

    public int getRuntime() {
        return Runtime;
    }

    public int getReleased_Year() {
        return Released_Year;
    }

    public float getIMDB_Rating() {
        return IMDB_Rating;
    }
}

public class MovieAnalyzer {

    List<Movie> movieList;
    List<String> genreList;

    public MovieAnalyzer(String datasetPath) {

        File csv = new File(datasetPath);
        movieList = new ArrayList<>();
        genreList = new ArrayList<>();
        try {
            BufferedReader textFile = new BufferedReader(new FileReader(csv, StandardCharsets.UTF_8));
            String line;
            String[] array;
            int arrayLen;
            StringBuilder sb;
            textFile.readLine(); //headline
            while ((line = textFile.readLine()) != null) {
                sb = new StringBuilder(line);
                {
                    char[] charArray = line.toCharArray();
                    int len = charArray.length;
                    boolean skip = false;
                    char c;
                    for (int i = 0; i < len; i++) {
                        c = charArray[i];
                        if (c == '"') {
                            skip = !skip;
                        }
                        if (!skip && c == ',') {
                            sb.replace(i, i + 1, "~");
                        }
                    }
                }
                array = sb.toString().split("~");
                arrayLen = array.length;

                String Series_Title = array[1].replace("\"", "");
                int Released_Year = Integer.parseInt(array[2]);
                String Certificate = array[3];
                int Runtime = Integer.parseInt(array[4].split(" ")[0]);
                List<String> Genre = List.of(array[5].replace("\"", "").split(", "));
                float IMDB_Rating = (Objects.equals(array[6], "") ? -1f : Float.parseFloat(array[6]));
                String Overview = array[7].startsWith("\"") ? array[7].substring(1, array[7].length() - 1) : array[7];
                int Meta_score = (Objects.equals(array[8], "") ? -1 : Integer.parseInt(array[8]));
                String Director = array[9];
                String Star1 = array[10], Star2 = array[11], Star3 = array[12], Star4 = array[13];
                long Noofvotes = Long.parseLong(array[14]);
                long Gross = (arrayLen == 15 ? -1L : Long.parseLong(array[15]
                        .replace("\"", "").replace(",", "")));
                Movie movie = new Movie(Series_Title, Released_Year, Certificate, Runtime, Genre,
                        IMDB_Rating, Overview, Meta_score, Director,
                        Star1, Star2, Star3, Star4, Noofvotes, Gross);
                movieList.add(movie);
                genreList.addAll(Genre);
            }
            textFile.close();
        } catch (FileNotFoundException e) {
            System.out.println("Not Found.");
        } catch (IOException e) {
            System.out.println("Some Wrongs.");
        }
    }

    public Map<Integer, Integer> getMovieCountByYear() {
        return movieList.stream().collect(Collectors.groupingBy(Movie::getReleased_Year,
                TreeMap::new, Collectors.summingInt(e -> 1))).descendingMap();
    }

    public Map<String, Integer> getMovieCountByGenre() {
        //todo//??
        TreeMap<String, Integer> res = new TreeMap<>();
        for (String genre : genreList) {
            res.put(genre, (int) (movieList.stream()
                    .filter(movie -> movie.getGenre().contains(genre)).count()));
        }

        return res.descendingMap();
    }

    public Map<List<String>, Integer> getCoStarCount() {
        Map<String, Integer> res = new HashMap<>();
        movieList.forEach(movie -> {
            List<String> list = List.of(
                    movie.getStarList().get(0).concat("~").concat(movie.getStarList().get(1)),
                    movie.getStarList().get(0).concat("~").concat(movie.getStarList().get(2)),
                    movie.getStarList().get(0).concat("~").concat(movie.getStarList().get(3)),
                    movie.getStarList().get(1).concat("~").concat(movie.getStarList().get(2)),
                    movie.getStarList().get(1).concat("~").concat(movie.getStarList().get(3)),
                    movie.getStarList().get(2).concat("~").concat(movie.getStarList().get(3)));
            list.forEach(names -> res.put(names, res.getOrDefault(names, 0) + 1));
        });
        return res.entrySet().stream().collect(Collectors.toMap(stringIntegerEntry ->
                Arrays.asList(stringIntegerEntry.getKey().split("~")), Map.Entry::getValue));
    }

    public List<String> getTopMovies(int top_k, String by) {
        switch (by) {
            case "runtime":
                return movieList.stream()
                        .sorted(Comparator.comparingInt(Movie::getRuntime).reversed()
                                .thenComparing(Movie::getSeries_Title))
                        .limit(top_k).map(Movie::getSeries_Title).toList();
            case "overview":
                return movieList.stream()
                        .sorted(Comparator.comparing(Movie::getOverview, (s1, s2) -> s2.length() - s1.length())
                                .thenComparing(Movie::getSeries_Title))
                        .limit(top_k).map(Movie::getSeries_Title).toList();
            default:
                System.out.println("Wrong in getTopMovies: by:" + by);
                return null;
        }
    }

    public List<String> getTopStars(int top_k, String by) {

        class InterEntry {
            String K;
            Double V;

            public InterEntry(String K, Double V) {
                this.K = K;
                this.V = V;
            }

            public Double getV() {
                return V;
            }

            public String getK() {
                return K;
            }
        }

        switch (by) {
            case "rating":
                Stream<InterEntry> s1 = movieList.stream()
                        .collect(Collectors.groupingBy(Movie::getStar1, Collectors.averagingDouble(Movie::getIMDB_Rating)))
                        .entrySet().stream()
                        .map(entry -> new InterEntry(entry.getKey(), entry.getValue()));
                Stream<InterEntry> s2 = movieList.stream()
                        .collect(Collectors.groupingBy(Movie::getStar2, Collectors.averagingDouble(Movie::getIMDB_Rating)))
                        .entrySet().stream()
                        .map(entry -> new InterEntry(entry.getKey(), entry.getValue()));
                Stream<InterEntry> s3 = movieList.stream()
                        .collect(Collectors.groupingBy(Movie::getStar3, Collectors.averagingDouble(Movie::getIMDB_Rating)))
                        .entrySet().stream()
                        .map(entry -> new InterEntry(entry.getKey(), entry.getValue()));
                Stream<InterEntry> s4 = movieList.stream()
                        .collect(Collectors.groupingBy(Movie::getStar4, Collectors.averagingDouble(Movie::getIMDB_Rating)))
                        .entrySet().stream()
                        .map(entry -> new InterEntry(entry.getKey(), entry.getValue()));

                Set<Map.Entry<String, Double>> resSet = Stream
                        .concat(s1, Stream.concat(s2, Stream.concat(s3, s4)))
                        .collect(Collectors.groupingBy(InterEntry::getK, Collectors.averagingDouble(InterEntry::getV)))
                        .entrySet();

                Stream<InterEntry> resS = resSet.stream()
                        .map(e -> new InterEntry(e.getKey(), e.getValue()))
                        .sorted(Comparator.comparingDouble(InterEntry::getV).reversed().thenComparing(InterEntry::getK));
                return resS.limit(top_k).map(InterEntry::getK).toList();

            case "gross":
                Stream<InterEntry> s5 = movieList.stream().filter(movie -> movie.getGross() != -1L)
                        .collect(Collectors.groupingBy(Movie::getStar1, Collectors.averagingLong(Movie::getGross)))
                        .entrySet().stream().map(e -> new InterEntry(e.getKey(), e.getValue()));
                Stream<InterEntry> s6 = movieList.stream().filter(movie -> movie.getGross() != -1L)
                        .collect(Collectors.groupingBy(Movie::getStar2, Collectors.averagingLong(Movie::getGross)))
                        .entrySet().stream().map(e -> new InterEntry(e.getKey(), e.getValue()));
                Stream<InterEntry> s7 = movieList.stream().filter(movie -> movie.getGross() != -1L)
                        .collect(Collectors.groupingBy(Movie::getStar3, Collectors.averagingLong(Movie::getGross)))
                        .entrySet().stream().map(e -> new InterEntry(e.getKey(), e.getValue()));
                Stream<InterEntry> s8 = movieList.stream().filter(movie -> movie.getGross() != -1L)
                        .collect(Collectors.groupingBy(Movie::getStar4, Collectors.averagingLong(Movie::getGross)))
                        .entrySet().stream().map(e -> new InterEntry(e.getKey(), e.getValue()));

                Set<Map.Entry<String, Double>> resSetg = Stream
                        .concat(s5, Stream.concat(s6, Stream.concat(s7, s8)))
                        .collect(Collectors.groupingBy(InterEntry::getK, Collectors.averagingDouble(InterEntry::getV)))
                        .entrySet();

                Stream<InterEntry> resS2g = resSetg.stream().map(e -> new InterEntry(e.getKey(), e.getValue()))
                        .sorted(Comparator.comparingDouble(InterEntry::getV).reversed().thenComparing(InterEntry::getK));
                return resS2g.limit(top_k).map(InterEntry::getK).toList();
            default:
                System.out.println("Wrong in getTopStars: by:" + by);
                return null;
        }
    }

    public List<String> searchMovies(String genre, float min_rating, int max_runtime) {
        return movieList.stream()
                .filter(movie -> movie.getGenre().contains(genre)
                        && movie.getIMDB_Rating() >= min_rating
                        && movie.getRuntime() <= max_runtime)
                .map(Movie::getSeries_Title).sorted().toList();
    }
}
