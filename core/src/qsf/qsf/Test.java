package qsf;

import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.data.DataModel;
import net.librec.data.model.ArffDataModel;
import net.librec.data.model.TextDataModel;
import net.librec.eval.RecommenderEvaluator;
import net.librec.eval.rating.RMSEEvaluator;
import net.librec.filter.GenericRecommendedFilter;
import net.librec.math.structure.SparseMatrix;
import net.librec.math.structure.SparseTensor;
import net.librec.math.structure.SparseVector;
import net.librec.recommender.Recommender;
import net.librec.recommender.RecommenderContext;
import net.librec.recommender.cf.ItemKNNRecommender;
import net.librec.recommender.cf.UserKNNRecommender;
import net.librec.recommender.cf.rating.FMSGDRecommender;
import net.librec.recommender.hybrid.HybridRecommender;
import net.librec.recommender.item.RecommendedItem;
import net.librec.similarity.PCCSimilarity;
import net.librec.similarity.RecommenderSimilarity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Qian Shaofeng
 *         created on 2017/9/22.
 */
public class Test {

    public static void test1() throws LibrecException {
        // specify the userIds and itemIds for filter
        ArrayList<String> userIdList = new ArrayList<>();
        ArrayList<String> itemIdList = new ArrayList<>();
        for (int i=1; i<=2; i++) {
            userIdList.add(Integer.toString(i));
            itemIdList.add(Integer.toString(4-i));
        }


     // generate recommendedList by recommender
        Configuration conf = new Configuration();
        Configuration.Resource resource = new Configuration.Resource("rec//cf//userknn-test.properties");
        conf.addResource(resource);
        conf.set("dfs.data.dir", "E:\\source-code\\librec-librec-src-v2.0\\core\\src\\main\\resources\\rec\\cf\\");
        conf.set("data.input.path", "userknn-test.properties");
        DataModel dataModel = new TextDataModel(conf);
        dataModel.buildDataModel();

        RecommenderContext context = new RecommenderContext(conf, dataModel);
        RecommenderSimilarity similarity = new PCCSimilarity();
        similarity.buildSimilarityMatrix(dataModel);
        context.setSimilarity(similarity);
        Recommender recommender = new UserKNNRecommender();
        recommender.setContext(context);
        recommender.recommend(context);
        List<RecommendedItem> recommendedItemList = recommender.getRecommendedList();

    // filter the recommendedList with GenericRecommendedFilter
        GenericRecommendedFilter filter = new GenericRecommendedFilter();
        filter.setUserIdList(userIdList);
        filter.setItemIdList(itemIdList);
        recommendedItemList = filter.filter(recommendedItemList);
    }

    public static void test2() throws LibrecException {
        // build data model
        Configuration conf = new Configuration();
        conf.set("dfs.data.dir", "data");
        conf.set("data.input.path", "test/arfftest/testset");
        conf.set("data.model.format", "arff");
        conf.set("data.splitter.trainset.ratio", "0.9");
        ArffDataModel dataModel = new ArffDataModel(conf);
        dataModel.buildDataModel();

        SparseTensor trainDataSet = (SparseTensor)dataModel.getTrainDataSet();
        System.out.println(trainDataSet);
        System.out.println("---------");
        SparseTensor testDataSet = (SparseTensor)dataModel.getTestDataSet();
        System.out.println(testDataSet);

        // build recommender context
        RecommenderContext context = new RecommenderContext(conf, dataModel);

        // build similarity
        conf.set("rec.recommender.similarity.key" ,"item");
        RecommenderSimilarity similarity = new PCCSimilarity();
        similarity.buildSimilarityMatrix(dataModel);
        context.setSimilarity(similarity);

        // build recommender
        conf.set("rec.neighbors.knn.number", "5");
        conf.set("rec.hybrid.lambda", "100");
//        conf.set("rec.recommender.isranking", "true");
//        Recommender recommender = new HybridRecommender();

        conf.set("rec.iterator.maximum", "10");
        conf.set("rec.factor.number", "10");
        conf.set("rec.iterator.learnRate", "0.01");
        Recommender recommender = new FMSGDRecommender();
        recommender.setContext(context);

        // run recommender algorithm
        recommender.recommend(context);

        // evaluate the recommended result
        RecommenderEvaluator evaluator = new RMSEEvaluator();
        System.out.println("RMSE:" + recommender.evaluate(evaluator));

        // set id list of filter
        List<String> userIdList = new ArrayList<>();
        List<String> itemIdList = new ArrayList<>();
//        userIdList.add("1");
//        userIdList.add("3");
//        userIdList.add("2");
        itemIdList.add("70");

        // filter the recommended result
        List<RecommendedItem> recommendedItemList = recommender.getRecommendedList();
        System.out.println(recommendedItemList.size());
        GenericRecommendedFilter filter = new GenericRecommendedFilter();
        filter.setUserIdList(userIdList);
        filter.setItemIdList(itemIdList);
        recommendedItemList = filter.filter(recommendedItemList);

        // print filter result
        for (RecommendedItem recommendedItem : recommendedItemList) {
            System.out.println(
                    "user:" + recommendedItem.getUserId() + " " +
                            "item:" + recommendedItem.getItemId() + " " +
                            "value:" + recommendedItem.getValue()
            );
        }
    }

    public static void main(String[] args) throws Exception {
        test2();
    }
}
