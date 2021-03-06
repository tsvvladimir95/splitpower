import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by vladimirtsvetkov on 09/12/14.
 */
public class Pow {
    public PairMatrix p;
    int k = 10000;

    boolean printFlag = true;//false;//

    public Pow(Matrix T) {
        p = dc_eig(T, 0.0001, k);
    }

    public PairMatrix dc_eig(Matrix T, double eps, int k) {
        PairMatrix QL = new PairMatrix(Matrix.identity(1), Matrix.identity(1));
        if (T.M == 1) { //if T is matrix of size 1x1

            if (printFlag) {
                System.out.println("NOT in else with T:");
                T.show();
                System.out.println();
            }

            QL.a = Matrix.identity(1);  //set Q
            QL.b = T;                   //set L
        } else {
            int div = T.M / 2;

            Matrix T1 = T.getBlock(0, div, 0, div);
            Matrix T2 = T.getBlock(div, T.M, div, T.M);

            if (printFlag) {
                System.out.println("in else");
            }

            double bm = T.GetElement(div - 1, div);
            T1.setElement(T1.M - 1, T1.M - 1, T1.GetElement(T1.M - 1, T1.M - 1) - bm);
            T2.setElement(0, 0, T2.GetElement(0, 0) - bm);

            if (printFlag) {
                System.out.println("before call will be div:" + div);

                System.out.println("---------\nT:");
                T.show();
                System.out.println("T1:");
                T1.show();
                System.out.println("T2:");
                T2.show();
                System.out.println("bm: " + bm);
                System.out.println("--------");

            }

            Pow pow1 = new Pow(T1);     //first call
            PairMatrix res1 = pow1.p;   //get Q1 and L1

            Pow pow2 = new Pow(T2);     //second call
            PairMatrix res2 = pow2.p;   //get Q2 and L2

            if (printFlag) {
                System.out.println("after call will be div:" + div);

                System.out.println("---------\nT:");
                T.show();
                System.out.println("T1:");
                T1.show();
                System.out.println("T2:");
                T2.show();
                System.out.println("bm: " + bm);
                System.out.println("--------");

                System.out.println("L1:");
                res1.b.show();
                System.out.println("L2:");
                res2.b.show();
            }

            //----------------------- build D ---------------------------------------------------
            //build D from L1 and L2;  D =  ( L1 0   )
            //                              ( 0  L2  )
            Matrix D = Matrix.Null(res1.b.M + res2.b.M, res1.b.M + res2.b.M);
            for (int i = 0; i < res1.b.M; i++)
                for (int j = 0; j < res1.b.M; j++)
                    D.setElement(i, j, res1.b.GetElement(i, j));
            for (int i = 0; i < res2.b.M; i++)
                for (int j = 0; j < res2.b.M; j++)
                    D.setElement(i + res1.b.M, j + res1.b.M, res2.b.GetElement(i, j));

            if (printFlag) {
                System.out.println("-----\nD is build from L1 & L2:");
                D.show();
            }

            //set elements, which are epsela-close to each other - equal
            for (int i = 0; i < (res1.b.M + res2.b.M); i++) {
                for (int j = 0; j < (res1.b.M + res2.b.M); j++) {
                    if ((i != j) && ((Math.abs(D.GetElement(i, i) - D.GetElement(j, j))) < eps)) {
                        D.setElement(j, j, D.GetElement(i, i));
                    }
                }
            }

            //----------------------- build u --------------------------------------------------
            Matrix Q1t = res1.a.transpose();
            Matrix Q2t = res2.a.transpose();

            if (printFlag) {
                System.out.println("check Q1t:");
                Q1t.show();
                System.out.println("check Q2t:");
                Q2t.show();
            }

            // (a column) u =   (   last column of Q1t  )
            //                  (   first column of Q2t )
            Matrix u = new Matrix(res1.b.M + res2.b.M, 1);
            //set last column of Q1t to u
            for (int i = 0; i < res1.b.M; i++) {
                if (Q1t.GetElement(i, 0) > eps) {
                    u.setElement(i, 0, Q1t.GetElement(i, Q1t.N - 1));
                }
            }
            //set first column of Q2t to u
            for (int i = 0; i < res2.b.M; i++) {
                //System.out.println("\t\tcur Q2^t(i,0) = " + Q2t.GetElement(i, 0) + "; eps=" + eps);
                if (Math.abs(Q2t.GetElement(i, 0)) > eps) {
                    //System.out.println("\t\tok");
                    u.setElement(i + res1.b.M, 0, Q2t.GetElement(i, 0));
                }
            }

            if (printFlag) {
                System.out.println("----\ncheck u: (build from Q1^t & Q2^t\n\tQ1:");
                res1.a.show();
                System.out.println("\tQ2:");
                res2.a.show();
                System.out.println("\tu:");
                u.show();
            }


            //---------------------- унулить маленькие числа ----------------------------------

            //---------------------- permutate --------------------------------------------------
            Matrix newu = new Matrix(u.data);
            Matrix newD = new Matrix(D.data);

            Double[] midarr = new Double[newD.M];//mas of diagonal elements of matrix D (=newD now)
            for (int i = 0; i < newD.M; i++) {
                midarr[i] = newD.GetElement(i, i);
            }
            Sorter s1 = new Sorter(midarr);
            s1.perfsort();  //now midarr is sorted(decrem)
            int[] perm1 = s1.perm;
            for (int i = 0; i < perm1.length; i++) {
                newD.setElement(i, i, midarr[i]);//s1.mas[i]);//
                newu.setElement(i, 0, u.GetElement(perm1[i], 0));
            }

            Matrix tempu = u;
            Matrix tempD = D;

            if (printFlag) {
                System.out.println("perm:");
                for (int i = 0; i < perm1.length; i++)
                    System.out.print(" "+ perm1[i]);
                System.out.println();
                System.out.println("-----\ncheck swaped u & D:\n\tD:");
                D.show();
                System.out.println("\tswaped D:");
                newD.show();
                System.out.println("\tu:");
                u.show();
                System.out.println("\tswaped u:");
                newu.show();
                System.out.println();
            }

            Matrix prevu = new Matrix(u.data);
            u = newu;
            Matrix prevD = new Matrix(D.data);
            D = newD;

            //----------------to vekovoe - u column and D diagonal---------------------------

            //CenturyEquation cq = new CenturyEquation(k, bm, kj, Dij);
            CenturyEquation cq = new CenturyEquation(k, bm, u, D);
            Double[] roots = cq.count(); //null;
            if (printFlag) {
                System.out.println("ROOTS:");
                for (int i = 0; i < roots.length; i++)
                    System.out.print(", " + roots[i]);
                System.out.println();
            }

        //------------------------- levner ----------------------------------------------------
            //will recieve new w

            //count k_d_ij fro all d_ij
            HashMap<Double, Integer> getkdij = new HashMap<Double, Integer>();
            for (int i = 0; i < res1.b.M + res2.b.M; i++) {
                if (!getkdij.containsKey(D.GetElement(i, i))) {
                    getkdij.put(D.GetElement(i, i), 1);
                } else {
                    getkdij.put(D.GetElement(i, i), getkdij.get(D.GetElement(i, i)) + 1);
                }
            }
            //count l_j for all d_ij
            HashMap<Double, Integer> getlj = new HashMap<Double, Integer>();
            for (Double key : getkdij.keySet()) {
                int lol = 0;
                for (double num : roots) {
                    if (key == num) {
                        lol++;
                    }
                }
                getlj.put(key, lol);
            }

            //this must be needless, as k_d_ij & l_j are only for d[i] from D
            /*for (Double root: roots) {
                if (!getkdij.containsKey(root))
                    getkdij.put(root, 0);
                if (!getlj.containsKey(root))
                    getlj.put(root, 0);
            }*/

            if (printFlag) {
                System.out.println("*****\ncheck kdij keys: " + getkdij.keySet());
                System.out.println(getkdij);
                System.out.println("*****\ncheck lj keys: " + getlj.keySet());
                System.out.println(getlj);
            }

            //---------------------------- count w ---------------------------------------------

            //count w so that: Spec(D + ww^t) = { alpha_i } (= roots)
            Double[] w = new Double[roots.length];
            for (int i = 0; i < roots.length; i++) {
                System.out.println("\t---count w[" + i + "]");
                //System.out.println("@@@ " + D.GetElement(i, i) + ", " + getkdij.keySet() + " ###");
                //System.out.println("@@ " + getkdij.get(D.GetElement(i, i)) + " ##");

                //Double  alpha_i = roots[i];
                Double  d_ij    = D.GetElement(i, i);
                Integer l_j     = getlj.get(d_ij);//getlj.get(alpha_i);
                Integer k_d_ij  = getkdij.get(d_ij);

                if (l_j == 0 && k_d_ij == 1) {
                    double nom = 1.0;
                    double denom = 1.0;
                    for (int j = 0; j < res1.b.M + res2.b.M; j++) {
                        //System.out.println("\troots[j] = " + roots[j]);
                        if (printFlag) {
                            System.out.println("\tnom *= " + roots[j] + " - " + d_ij);
                        }
                        nom *= roots[j] - d_ij;
                    }
                    for (int j = 0; j < res1.b.M + res2.b.M; j++) {
                        if (D.GetElement(j, j) != d_ij) {
                            //System.out.println("\tD[j,j] = " + D.GetElement(j, j));
                            if (printFlag) {
                                System.out.println("\tdenom *= " + D.GetElement(j, j) + " - " + d_ij);
                            }
                            denom *= D.GetElement(j, j) - d_ij;
                        }
                    }

                    if (true) {
                        System.out.println("\td_ij = " + d_ij);
                        System.out.println("\tcheck nom: " + nom);
                        System.out.println("\tcheck denom: " + denom);
                    }

                    w[i] = Math.sqrt(nom / denom);
                } else if (l_j == (k_d_ij - 1) && k_d_ij-1 > 0) {
                    double nom = 1.0;
                    double denom = 1.0;
                    for (int j = 0; (j < res1.b.M + res2.b.M); j++) {
                        if (roots[j] != d_ij) {//D.GetElement(j, j)) {
                            nom *= roots[j] - d_ij;//D.GetElement(j, j);
                        }
                    }
                    denom *= k_d_ij;
                    for (int j = 0; (j < res1.b.M + res2.b.M); j++) {
                        //if (D.GetElement(j, j) != D.GetElement(i, i)) {
                        if (d_ij != D.GetElement(j, j)) {
                            //denom *= getkdij.get(D.GetElement(j, j)) * (D.GetElement(j, j) - D.GetElement(i, i));
                            denom *= (D.GetElement(j, j) - d_ij);
                        }
                    }
                    if (true) {
                        System.out.println("\td_ij = " + d_ij);
                        System.out.println("\tcheck nom: " + nom);
                        System.out.println("\tcheck denom: " + denom);
                    }

                    w[i] = Math.sqrt(nom / denom);
                } else if (k_d_ij == 1 && l_j >= k_d_ij) {
                    w[i] = 0.0;
                } else if (l_j > (k_d_ij - 1) && k_d_ij-1 > 0) {
                    w[i] = 0.0;
                } else {
                    System.out.println("!! ERROR while counting w[" + i + "]: special case");
                }
            }

            if (printFlag) {//(false) {//
                //D.show();
                System.out.println("w:");
                for (int i = 0; i < w.length; i++) {
                    System.out.print(w[i] + ", ");
                }
                System.out.println("\nend w");
            }

            if (printFlag) {
                System.out.println("Ensure that Spec(D + w*wt) = {roots}");
                Matrix wmatrtr = Matrix.rowToColumn(w);
                Matrix wmatr = wmatrtr.transpose();
                //D.show();
                //wmatr.show();
                Matrix forcheck = D.plus(wmatrtr.times(wmatr));

                System.out.println("Start (D + w*wt)");
                forcheck.show2();
                System.out.println("End (D + w*wt)");

            }

            //-----------------------------------------------------------------------------------

            //d_ij - different elements from {d[i] | D = diag{d[0], ..., d[n] }
            ArrayList<Double> dijarr = new ArrayList<Double>();
            //matches d_ij to its indexes in D
            HashMap<Double, ArrayList<Integer>> helpmap = new HashMap<Double, ArrayList<Integer>>();
            for (int i = 0; i < D.M; i++) {
                if (!dijarr.contains(D.GetElement(i, i))) {
                    dijarr.add(D.GetElement(i, i));
                    ArrayList<Integer> qq = new ArrayList<Integer>();
                    qq.add(i);
                    helpmap.put(D.GetElement(i, i), qq);
                } else {
                    ArrayList<Integer> qq = helpmap.get(D.GetElement(i, i));
                    qq.add(i);
                    helpmap.put(D.GetElement(i, i), qq);
                }
            }

            Matrix Dij = Matrix.Null(dijarr.size(), dijarr.size());
            for (int i = 0; i < dijarr.size(); i++) {
                Dij.setElement(i, i, dijarr.get(i));
            }

            Matrix kj = new Matrix(dijarr.size(), 1);
            for (int i = 0; i < dijarr.size(); i++) {
                for (int j : helpmap.get(dijarr.get(i))) {
                    kj.setElement(i, 0, kj.GetElement(i, 0) + u.GetElement(j, 0) * u.GetElement(j, 0));
                }
            }

            //---------------------count eigenvectors using w-------------------------------------------

            //Matrix eigenvector = Matrix.Hilbert(1, 1);
            //eigenvector = null;
            //double[][] ismQ1 = new double[res1.b.M + res2.b.M][res1.b.M + res2.b.M];

            //creating of eigenvectors matrix
            ArrayList<Matrix> eigenvectors = new ArrayList<Matrix>();

            for (int z = 0; z < roots.length; z++) {

                if (!dijarr.contains(roots[z])) {
                    Matrix eigenvector = D.minus(Matrix.identity(D.M).muldig(roots[z])).degMin1().times(Matrix.rowToColumn(w));
                    //eigenvector = D.minus(Matrix.identity(D.M).muldig(roots[z])).degMin1().times(u);
                    if (printFlag) {
                        System.out.println("\nadd eigenvector with case 0");
                        eigenvector.show();
                    }
                    Matrix normedeigenvector = eigenvector;//D.minus(Matrix.identity(D.M).muldig(roots[z])).degMin1().times(Matrix.rowToColumn(w));
                    //normedeigenvector = normedeigenvector.getNormed();
                    if (printFlag) {
                        System.out.println("\nadd normed eigenvector with case 0");
                        normedeigenvector.show();
                    }
                    eigenvectors.add(normedeigenvector);
                    //eigenvectors.add(eigenvector);
                } else {
                    if (getlj.get(roots[z]) == (getkdij.get(roots[z]) - 1)) {
                        ArrayList<Integer> st = helpmap.get(roots[z]);
                        for (Integer s : st) {
                            for (Integer t : st) {
                                if (s != t) {
                                    Matrix eigenvector1 = Matrix.identity(roots.length).getBlock(0, roots.length, t, t + 1).muldig(w[s]);
                                    Matrix eigenvector2 = Matrix.identity(roots.length).getBlock(0, roots.length, s, s + 1).muldig(w[t]);
                                    Matrix eigenvector = eigenvector1.minus(eigenvector2);
                                    if (printFlag) {
                                        System.out.println("add eigenvector with case 1");
                                    }
                                    eigenvectors.add(eigenvector);
                                }
                            }
                        }
                    } else if (getlj.get(roots[z]) == (getkdij.get(roots[z]))) {
                        ArrayList<Integer> idx = helpmap.get(roots[z]);
                        for (Integer id : idx) {
                            Matrix eigenvector = Matrix.identity(roots.length).getBlock(0, roots.length, id, id + 1);
                            if (printFlag) {
                                System.out.println("add eigenvector with case 2");
                            }
                            eigenvectors.add(eigenvector);
                        }
                    } else if (getlj.get(roots[z]) == (getkdij.get(roots[z]) + 1)) {
                        ArrayList<Integer> idx = helpmap.get(roots[z]);
                        for (Integer id : idx) {
                            Matrix eigenvector = Matrix.identity(roots.length).getBlock(0, roots.length, id, id + 1);
                            if (printFlag) {
                                System.out.println("add eigenvector with case 3");
                            }
                            eigenvectors.add(eigenvector);
                        }
                        Double[] zz = new Double[roots.length];
                        for (Integer f = 0; f < zz.length; f++) {
                            if (tempD.GetElement(f, f) == dijarr.get(f)) {
                                zz[f] = 0.0;
                            } else {
                                zz[f] = (1 / (tempD.GetElement(f, f) - dijarr.get(f))) * w[f];
                            }
                        }
                        Matrix eigenvector = Matrix.rowToColumn(zz);
                        if (printFlag) {
                            System.out.println("add eigenvector with case 4");
                        }
                        eigenvectors.add(eigenvector);
                    }
                }
                //eigenvector = D.minus(Matrix.identity(D.M).muldig(roots[z])).degMin1().times(Matrix.rowToColumn(w));
                //for(int y = 0; y < res1.b.M + res2.b.M; y++) {
                //    ismQ1[y][z] = eigenvector.GetElement(y, 0);
                //}


            }

            if (printFlag) {
                System.out.println("\nstart found eigenvectors:");
                for (Matrix vector : eigenvectors) {
                    vector.show2();
                }
                System.out.println("end found eigenvectors:");
            }

            //-------------------------- set blocks ------------------------------------------------

            //create L - diagonal matrix with eigenvalues (roots of century equation)
            Matrix L = Matrix.rowToDiag(roots);


            //build Q' - matrix of eigenvectors
            Double[][] ismQ1 = new Double[roots.length][roots.length];
            System.out.println("\n\t---start build Q'----");
            for (int i = 0; i < ismQ1.length; i++) {
                for (int j = 0; j < ismQ1[i].length; j++) {
                    ismQ1[i][j] = 0.0;
                    //System.out.print(" " + ismQ1[i][j]);
                }
                //System.out.println();
            }

            for (int i = 0; i < roots.length; i++) {
                for (int j = 0; j < eigenvectors.get(i).M; j++) {
                    ismQ1[j][i] = eigenvectors.get(i).GetElement(j, 0);
                }
            }

            //Q111 is Q' - matrix of eigenvectors
            Matrix Q111 = new Matrix(ismQ1);

            //--swap Q' or not? consideration that Q' was counted using swaped matrixes---------------------
            Matrix swapedBackQshtrih = new Matrix(Q111);
            if (true) {
                for (int i = 0; i < perm1.length; i++) {
                    for (int j = 0; j < Q111.M; j++)//perm1.length; j++)
                        swapedBackQshtrih.setElement(j, i, Q111.GetElement(j, perm1[i]));
                }
            }

            if (printFlag) {
                System.out.println("check D + bm*uu^t (must be = T)");
                D.plus(u.times(u.transpose()).muldig(bm)).show();
                System.out.println("number of roots: " + roots.length + "\nnumber of eigenvectors: " + eigenvectors.size());
                System.out.println("---check eigenvectors:");
                for (int i = 0; i < eigenvectors.size(); i++) {
                    System.out.println("\teigenvector [" + i + "] for alpha = " + roots[i]);
                    eigenvectors.get(i).show();
                }
                System.out.println("---check Q' (matrix of eigevectors):");
                Q111.show();
                System.out.println("and swaped back Q':");
                swapedBackQshtrih.show();
            }

            //--------------count Q = ( (Q1 0) (0 Q2) ) * Q' and swap it (if needed?)---------------
            Matrix Q = new Matrix(Q1t.M + Q2t.M, Q1t.N + Q2t.N);
            for (int z = 0; z < Q1t.M; z++) {
                for (int zz = 0; zz < Q1t.N; zz++)
                    Q.setElement(z, zz, Q1t.GetElement(z, zz));
            }
            for (int z = 0; z < Q2t.M; z++) {
                for (int zz = 0; zz < Q2t.N; zz++)
                    Q.setElement(z + Q1t.M, zz + Q1t.N, Q2t.GetElement(z, zz));
            }
            Q = Q.transpose();//because it was build from transposed matrixes - so return it back

            //Qfinal = Qfinal.transpose();


            if (printFlag) {
                System.out.println("\n ---check Q = ( (Q1, 0) (0, Q2) ):");
                System.out.println("Q1: ");
                res1.a.show();
                System.out.println("Q2: ");
                res2.a.show();
                System.out.println("Q: ");
                Q.show();
            }

            if (printFlag) {
                System.out.println("check Q before swap:");
                Q.show();
            }

            //---count-swaped--Qfinal-----swap or not???---------------------------------------------
            Matrix swapedQfinal = new Matrix(Q);
            for (int i = 0; i < perm1.length; i++) {
                for (int j = 0; j < perm1.length; j++)
                    swapedQfinal.setElement(j, i, Q.GetElement(j, perm1[i]));
            }
            Q = swapedQfinal;

            //Q = Q.transpose();

            if (printFlag) {
                System.out.println("check Q after swap:");
                Q.show();
            }

            //-------what is it???---------------------------------------

            Matrix saveQ = new Matrix(Q.data);

            Matrix saveQ111 = new Matrix(Q111.data);
            saveQ111 = saveQ111.transpose();

            Matrix newsaveQ111 = new Matrix(saveQ111.data);
            for (int i = 0; i < perm1.length; i++) {
                for (int j = 0; j < perm1.length; j++)
                    newsaveQ111.setElement(j, i, saveQ111.GetElement(j, perm1[i]));
            }
            saveQ111 = newsaveQ111;

            if (false) {
                System.out.println("\n\t\tQ:");
                saveQ.show();
                System.out.println("\n\t\tQ':");
                saveQ111.show();
            }
            //-----------count Qfinal = ((Q1 0) (0 Q2)) * Q' ------------------------------------------------


            //Matrix Qfinal = saveQ.times(saveQ111);
            Matrix Qfinal = Q.times(Q111);

            //Matrix Qfinal = Q.times(swapedBackQshtrih);

            //-------------for-check--------------------
            Matrix Qprev = new Matrix(Q.data);
            Qprev = Qprev.transpose();

            Matrix newQprev = new Matrix(Qprev.data);
            for (int i = 0; i < perm1.length; i++) {
                for (int j = 0; j < perm1.length; j++)
                    newQprev.setElement(j, i, Qprev.GetElement(j, perm1[i]));
            }


            if (false) {
                System.out.println("check u:");
                System.out.println("Q1^t:");
                res1.a.transpose().show();

                System.out.println("Q2^t:");
                res2.a.transpose().show();

                System.out.println("for Q^t * v: div = " + div);
                System.out.println("u:");
                u.show();
            }
            if (printFlag) {
                System.out.println("\n ---check swapedQ = swapped ( (Q1, 0) (0, Q2) ) :");
                swapedQfinal.show();

                System.out.println(" ---check currentQ * Q':");
                Qfinal.show();
            }
            if (printFlag) {
                System.out.println("check L before:");
                L.show();
                System.out.println("check Qfinal before:");
                Qfinal.show();
                System.out.println("check perm1:");
                for (int w1 = 0; w1 < perm1.length; w1++) {
                    System.out.print(perm1[w1] + ", ");
                }
            }

            //boolean acceptchanges = true;

            //----------try-to--swap--different matrixes--to-get--needed---result------------------

            if (false) {
                for (int i = 0; i < perm1.length / 2; i++) {
                    double temp = L.data[i][i];
                    L.data[i][i] = L.data[perm1[i]][perm1[i]];
                    L.data[perm1[i]][perm1[i]] = temp;
                }
            }
            if (false) {
                Matrix newL = new Matrix(L);
                for (int i = 0; i < perm1.length; i++)
                    newL.setElement(i, i, L.GetElement(perm1[i], perm1[i]));
                L = newL;
            }

            if (false) {
                Matrix newQfinal = new Matrix(Qfinal);
                for (int i = 0; i < perm1.length; i++) {
                    for (int j = 0; j < perm1.length; j++)
                        newQfinal.setElement(j, i, Qfinal.GetElement(j, perm1[i]));
                }
                Qfinal = newQfinal;
            }

            if (false) {
                Qfinal = Qfinal.transpose();
            }

            //-------------different-----checks-------------------------------------------------

            if (printFlag) {
                System.out.println("check L after:");
                L.show();
                System.out.println("check Qfinal after:");
                Qfinal.show();
            }

            if (printFlag) {
                //System.out.println("\n ---check T = Q * LLL * Q^(-1) :");
                //Matrix Tcheck = Qfinal.times(LLL).times(Qfinal.degMin1());
                System.out.println("\n ---check T = Q * L * Q^(-1) :");
                T.show();
                //Matrix Tcheck = Qfinal.times( D.plus(u.times(u.transpose()).muldig(bm))).times(Qfinal.transpose());
                Matrix Tcheck = Qfinal.times(L).times(Qfinal.degMin1());
                Tcheck.show();



                System.out.println("\n chec for swapped T = Q (D + bmuut) Qt");
                Matrix Tcheck2 = Qprev.times(D.plus(u.times(u.transpose()).muldig(bm))).times(Qprev.transpose());
                Tcheck2.show();
                System.out.println("\n chec T = Q (D + bmuut) Qt");
                Matrix Tcheck21 = newQprev.times(prevD.plus(prevu.times(prevu.transpose()).muldig(bm))).times(newQprev.transpose());
                Tcheck21.show();

                System.out.println("\n check for swapped T = Q (D + wwt) Qt");
                Matrix wmatr = Matrix.rowToColumn(w);
                Matrix Tcheck3 = Qprev.times(D.plus(wmatr.times(wmatr.transpose())).times(Qprev.transpose()));
                Tcheck3.show();
                System.out.println("\n L:");
                L.show();
                Qprev.show();
                newQprev.show();

                System.out.println("\n\n========================\n\none more check:\n");

                for (int i = 0; i < perm1.length; i++){
                    Matrix vector = new Matrix(Q.M, 1);
                    for (int j = 0; j < Q.M; j++)
                        vector.setElement(j, 0, Qfinal.GetElement(j, i));
                    System.out.println("vector:");
                    vector.show();

                    System.out.println("T * eigenvector[" + i + "] = ");
                    //T.times(eigenvectors.get(i)).show();
                    T.times(vector.getNormed()).show();

                    System.out.println("must be equal to "+L.GetElement(i,i)+"* same eigenvector = ");
                    vector.getNormed().mulOnConstant(L.GetElement(i,i)).show();

                    System.out.println();
                }
                System.out.println("\n\n=======================================");
            }

            //----------finished-checks-----return--results--------------------------

            QL.a = Qfinal;//Q111;//
            QL.b = L;

            if (printFlag) {
                System.out.println("\n\t\t\tinPowfirst:\n");
                QL.a.show();
                System.out.println("\n\t\t\tinPowsecond:\n");
                QL.b.show();
            }
        }
        return QL;
    }
}