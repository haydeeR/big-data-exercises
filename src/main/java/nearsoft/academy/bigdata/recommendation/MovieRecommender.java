package nearsoft.academy.bigdata.recommendation;

import com.sun.xml.internal.xsom.impl.scd.Iterators;
import jdk.nashorn.internal.runtime.events.RecompilationEvent;
import org.apache.commons.collections.MapUtils;
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
import java.util.*;
import java.util.zip.GZIPInputStream;

public class MovieRecommender {

    private DataModel model = null;
    private UserSimilarity similarity = null;
    private UserNeighborhood neighborhood = null;
    private UserBasedRecommender recommender = null;
    private Writer writer = null;
    private long totalReviews = 0;
    private Map<String, Long> mapOfUsers;
    private Map<String, Long> mapOfProducts;
    private Map<Long, String> arrayValuesOfProducts;

    public MovieRecommender(String nameFile) throws IOException, TasteException {
        FileInputStream bais = new FileInputStream(nameFile);
        GZIPInputStream gzis = new GZIPInputStream(bais);
        InputStreamReader reader = new InputStreamReader(gzis);
        BufferedReader in = new BufferedReader(reader);
        OutputStream formattedFile = new FileOutputStream("test.txt");
        writer = new PrintWriter(formattedFile);
        this.mapOfProducts = new HashMap<String, Long>();
        this.mapOfUsers = new HashMap<String, Long>();
        this.arrayValuesOfProducts = new HashMap<Long, String>();


        String readed;
        long i = 0, j = 0;
        long idUser = 0,idProduct = 0;
        float score = -1.0f;

        while ((readed = in.readLine()) != null ) {
            if(readed.isEmpty() == false){
                if(readed.contains("/productId:")) {
                    idProduct = saveOneReviewInMap(getStringToWrite(readed));
                }else if(readed.contains("/userId:")){
                    idUser = saveUserRevieInMap(getStringToWrite(readed));
                }else if(readed.contains("/score:")){
                    score = Float.parseFloat(getStringToWrite(readed));
                }
            }else if(idProduct > 0 && idUser > 0 && score >= 0)
            {
                writeInFile(idProduct+","+idUser+","+score+"\n");
                i=-1;
                j++;
                idProduct = idUser = -1;
                score = -1f;
            }
            i++;
        }
        this.writer.close();
        in.close();
        this.totalReviews = j;
        this.model = new FileDataModel(new File("test.txt"),true,60000L);
        this.similarity = new PearsonCorrelationSimilarity(model);
        this.neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        this.recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

    }

    public long saveOneReviewInMap(String oneRegister){
        long productId = -1;
        if(this.mapOfProducts.size() > 0 && this.mapOfProducts.containsKey(oneRegister)){
            productId =  this.mapOfProducts.get(oneRegister);
        }else{
            productId = Long.parseLong((this.mapOfProducts.size()+1)+"");
            this.mapOfProducts.put(oneRegister,productId);
            arrayValuesOfProducts.put(productId,oneRegister);
        }
        return productId;
    }

    public long saveUserRevieInMap(String oneRegister){
        long userId = -1;
        if(this.mapOfUsers.size() > 0 && this.mapOfUsers.containsKey(oneRegister)){
            userId = this.mapOfUsers.get(oneRegister);
        }else{
            userId = Long.parseLong((this.mapOfUsers.size()+1)+"");
            this.mapOfUsers.put(oneRegister,userId);
        }
        return userId;
    }

    public void writeInFile(String stringTowrite) throws IOException {
        writer.write(stringTowrite);
    }

    public String getStringToWrite(String stringToWrite){
        String[] arrayOfStrings = stringToWrite.split(" ");
        String data = "0";
        if(arrayOfStrings.length > 1 )
            data = arrayOfStrings[1];
        return data;
    }

    public static void main(String[] args) throws IOException, TasteException {

    }

    public List<String> getRecommendationsForUser(String userkey) throws TasteException {
        long userValue = 0;
        List<String> usersRecommendedString = new ArrayList<String>();

        if(this.mapOfUsers.containsKey(userkey))
            userValue = this.mapOfUsers.get(userkey);
        List<RecommendedItem> recomendations = this.recommender.recommend(userValue,3);
        usersRecommendedString.add(this.arrayValuesOfProducts.get(recomendations.get(0).getItemID()));
        usersRecommendedString.add(this.arrayValuesOfProducts.get(recomendations.get(1).getItemID()));
        usersRecommendedString.add(this.arrayValuesOfProducts.get(recomendations.get(2).getItemID()));

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
