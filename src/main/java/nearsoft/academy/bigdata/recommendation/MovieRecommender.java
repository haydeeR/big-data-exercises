package nearsoft.academy.bigdata.recommendation;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class MovieRecommender {

    private DataModel model = null;
    private UserSimilarity similarity = null;
    private UserNeighborhood neighborhood = null;
    private UserBasedRecommender recommender = null;
    private long totalReviews = 0;
    private Map<String, Long> mapOfUsers;
    private Map<String, Long> mapOfProducts;

    public MovieRecommender(String nameFile) throws IOException, TasteException {
        FileInputStream bais = new FileInputStream(nameFile);
        GZIPInputStream gzis = new GZIPInputStream(bais);
        InputStreamReader reader = new InputStreamReader(gzis);
        BufferedReader in = new BufferedReader(reader);
        this.mapOfProducts = new HashMap<String, Long>();
        this.mapOfUsers = new HashMap<String, Long>();


        String readed;
        String readOneReview = "";
        BigInteger toWrite;
        long i = 0, j = 0;
        long idUser = 1;
        long idProduct = 1;

        //System.out.println(in.readLine());
        while ((readed = in.readLine()) != null && j<50) {
            if(readed.isEmpty() == false){
                if(i == 0 || i == 1 || i == 4)
                {
                    if(i == 0) {
                        idProduct = saveOneReviewInMap(getStringToWrite(readed));
                        writeInFile(idProduct,",");
                    }
                    if(i == 1){
                        idUser = saveUserRevieInMap(getStringToWrite(readed));
                        writeInFile(idUser,",");
                    }
                    if(i == 4){
                        writeInFile(readed);
                    }
                }
            }else{
                writeInFile("\n \n");
                i=-1;
                j++;
            }
            i++;
        }
        in.close();
        this.totalReviews = --j;
/*

        this.model = new FileDataModel(new File("test.txt"),true,60000L);
        this.similarity = new PearsonCorrelationSimilarity(model);
        this.neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        this.recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
        */


    }

    public long saveOneReviewInMap(String oneRegister){
        long idProduct = 0, productId = 0;
        if(this.mapOfProducts.size() > 0 && this.mapOfProducts.containsKey(oneRegister)){
            return this.mapOfProducts.get(oneRegister);
        }else{
            productId = Long.parseLong((this.mapOfProducts.size()+1)+"");
            this.mapOfProducts.put(oneRegister,productId);
        }
        return productId;
    }

    public long saveUserRevieInMap(String oneRegister){
        long userId = 0;
        if(this.mapOfUsers.size() > 0 && this.mapOfUsers.containsKey(oneRegister)){
            return this.mapOfUsers.get(oneRegister);
        }else{
            userId = Long.parseLong((this.mapOfUsers.size()+1)+"");
            this.mapOfUsers.put(oneRegister,userId);
        }
        return userId;
    }

    public void writeInFile(String stringTowrite) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("test.txt", true));
        stringTowrite = getStringToWrite(stringTowrite);
        writer.append(stringTowrite);
        writer.close();
    }

    public void writeInFile(Long data, String comma) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("test.txt", true));
        writer.append(data.toString()+comma);
        writer.close();
    }

    public String getStringToWrite(String stringToWrite){
        String[] arrayOfStrings = stringToWrite.split(" ");
        String data = arrayOfStrings[1];
        return data;
    }

    public String[] getItemsOfStringToWrite(String stringToWrite){
        String[] arrayOfStrings = stringToWrite.split(" ");
        return arrayOfStrings;
    }

    public static void main(String[] args) throws IOException, TasteException {

        String key = "A141HP4LYPWMSR";
        BigInteger productId = new BigInteger(key.getBytes());
        System.out.println(productId);
        System.out.println(new String(productId.toByteArray())); // prints "A141HP4LYPWMSR"

        /*

        DataModel model = new FileDataModel(new File("data/dataset.csv"));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        List<RecommendedItem> recommendations = recommender.recommend(2 , 5);
        for (RecommendedItem recommendation : recommendations) {
            System.out.println(recommendation);
        }
        */

    }

    public List<String> getRecommendationsForUser(String idUser) throws TasteException {
        long userkey = 0;
        List<String> usersRecommendedString = new ArrayList<String>();
        for (Map.Entry<String, Long> entry : this.mapOfUsers.entrySet()) {
            if (Objects.equals(idUser, entry.getKey())) {
                userkey = entry.getValue();
                break;
            };
        }
        List<RecommendedItem> recomendations = this.recommender.recommend(userkey,3);
        for (RecommendedItem recomendation : recomendations) {
            System.out.println(recomendation);
        }
        return usersRecommendedString;
    }

    public long getTotalReviews() throws TasteException {
        long total = 0;
            total = this.totalReviews;
        return total;
    }

    public long getTotalProducts() throws TasteException {
        long totalProducts = 0;
            totalProducts = this.model.getNumItems();
        return totalProducts;
    }

    public long getTotalUsers() throws TasteException {
        long totalUsers = 0;
            totalUsers = this.model.getNumUsers();
        return totalUsers;
    }
}
