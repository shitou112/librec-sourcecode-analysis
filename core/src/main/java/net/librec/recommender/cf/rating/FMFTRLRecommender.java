package net.librec.recommender.cf.rating;

import net.librec.common.LibrecException;
import net.librec.math.structure.DenseMatrix;
import net.librec.math.structure.DenseVector;
import net.librec.math.structure.SparseVector;
import net.librec.math.structure.TensorEntry;
import net.librec.recommender.FactorizationMachineRecommender;

/**
 * Factorization Machine Recommender via Follow The Regularized Leader
 *
 * http://castellanzhang.github.io/2016/10/16/fm_ftrl_softmax
 * @author Qian Shaofeng
 *
 */
public class FMFTRLRecommender extends FactorizationMachineRecommender{

    // L1 regularization
    private double lambda1W0, lambda1W, lambda1V;

    // L2 regularization
    private double lambda2W0, lambda2W, lambda2V;

    // alpha, beta
    private double alphaW0, alphaW, alphaV;
    private double betaW0, betaW, betaV;

    @Override
    protected void setup() throws LibrecException {
        super.setup();
        lambda1W0 = conf.getDouble("rec.regularization.lambda1W0");
        lambda1W = conf.getDouble("rec.regularization.lambda1W");
        lambda1V = conf.getDouble("rec.regularization.lambda1V");

        lambda2W0 = conf.getDouble("rec.regularization.lambda2W0");
        lambda2W = conf.getDouble("rec.regularization.lambda2W");
        lambda2V = conf.getDouble("rec.regularization.lambda2V");

        alphaW0 = conf.getDouble("rec.alphaW0");
        alphaW = conf.getDouble("rec.alphaW");
        alphaV = conf.getDouble("rec.alphaV");
        betaW0 = conf.getDouble("rec.betaW0");
        betaW = conf.getDouble("rec.betaW");
        betaV = conf.getDouble("rec.betaV");
    }

    @Override
    protected void trainModel() throws LibrecException {
        if (!isRanking){
            buildRatingModel();
        }
    }

    private void buildRatingModel() throws LibrecException {
        double zW0 = 0;
        DenseVector zW = new DenseVector(p);
        DenseMatrix zV = new DenseMatrix(p, k);
        zW.init(0);
        zV.init(0);

        double nW0 = 0;
        DenseVector nW = new DenseVector(p);
        DenseMatrix nV = new DenseMatrix(p, k);
        nW.init(0);
        nV.init(0);

        double gW0, thetaW0;

        DenseVector gW = new DenseVector(p);
        DenseVector thetaW = new DenseVector(p);

        DenseMatrix gV = new DenseMatrix(p, k);
        DenseMatrix thetaV = new DenseMatrix(p, k);

        for (int iter=0; iter < numIterations; ++iter){
            loss = 0.0;
            int userDimension = trainTensor.getUserDimension();
            int itemDimension = trainTensor.getItemDimension();
            for (TensorEntry me: trainTensor) {
                int[] entryKeys = me.keys();
                SparseVector x = tenserKeysToFeatureVector(entryKeys);
                double rate = me.get();

                // compute rating value
                double pred = predict(entryKeys[userDimension], entryKeys[itemDimension], x);

                double err = pred - rate;
                loss += err * err;

                // loss gradient, loss = 1/2 * (yhat - y)^2
                double gradLoss = err;

                // compute w0 gradient
                gW0 = gradLoss;
                thetaW0 = 1 / alphaW0 * (Math.sqrt(nW0 + Math.pow(gW0, 2)) - Math.sqrt(nW0));
                zW0 += gW0 - thetaW0 * w0;
                nW0 += Math.pow(gW0, 2);

                // update w0
                if (Math.abs(zW0) <= lambda1W0) {
                    w0 = 0;
                } else {
                    w0 = -1 / ((betaW0 + Math.sqrt(nW0)) / alphaW0 + lambda2W0) * (zW0 - sgn(zW0) * lambda1W0);
                }

                for (int l = 0; l < p; ++l) {
                    // compute W gradient
                    gW.set(l, gradLoss * x.get(l));
                    thetaW.set(l, 1 / alphaW * (Math.sqrt(nW.get(l) + Math.pow(gW.get(l), 2)) - Math.sqrt(nW.get(l))));
                    zW.add(l, gW.get(l) - thetaW.get(l) * W.get(l));
                    nW.add(l, Math.pow(gW.get(l), 2));

                    // update W
                    if (Math.abs(zW.get(l)) <= lambda1W) {
                        W.set(l, 0);
                    } else {
                        double value = -1 / ((betaW + Math.sqrt(nW.get(l))) / alphaW + lambda2W) * (zW.get(l) - sgn(zW.get(l)) * lambda1W);
                        W.set(l, value);
                    }

                    for (int f = 0; f < k; ++f) {
                        double hVlf = 0;
                        double xl = x.get(l);
                        if (xl != 0) {
                            hVlf = xl * (V.column(f).inner(x) - xl);
                        }

                        // compute V gradient
                        double gradVlf = gradLoss * hVlf;

                        gV.set(l, f, gradVlf);
                        thetaV.set(l, f, 1 / alphaV * (Math.sqrt(nV.get(l, f) + Math.pow(gV.get(l, f), 2)) - Math.sqrt(nV.get(l, f))));
                        zV.add(l, f, gV.get(l, f) - thetaV.get(l, f) * V.get(l, f));
                        nV.add(l, f, Math.pow(gV.get(l, f), 2));

                        // update V
                        if (Math.abs(zV.get(l, f)) <= lambda1V) {
                            V.set(l, f, 0);
                        } else {
                            double value = -1 / ((betaV + Math.sqrt(nV.get(l, f))) / alphaV + lambda2V) * (zV.get(l, f) - sgn(zV.get(l, f)) * lambda1V);
                            V.set(l, f, value);
                        }
                    }
                }
            }
            if (isConverged(iter)  && earlyStop)
                break;

        }
        System.out.println(w0);
        System.out.println(W);
        System.out.println("------");
        System.out.println(V);
    }

    private int sgn(double value){
        return value > 0? 1: value==0? 0 : -1;
    }

    @Override
    protected double predict(int userIdx, int itemIdx) throws LibrecException {
        return 0;
    }
}
