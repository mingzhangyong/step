package algorithm;

/**
 * @author: mingzhangyong
 * @create: 2021-10-08 10:19
 **/
public class BackspaceCompare {
    public boolean backspaceCompare(String s, String t) {

        int i = s.length()-1;
        int j = t.length() - 1;
        StringBuilder builderS = new StringBuilder();
        StringBuilder builderT = new StringBuilder();

        StringBuilder ss = new StringBuilder(s);
        StringBuilder tt = new StringBuilder(t);
        boolean result = true;

        int toDeleteS = 1;
        int toDeleteT = 1;
        while (i >= 0 || j >=0){
            if(i >= 0){
                char chsi = ss.charAt(i);

                if(chsi != '#'){
                    builderS.append(chsi);
                }else{
                    if(i > 0){
                        if(ss.charAt(i-1) != '#'){
                            int start = Math.max(i - toDeleteS, 0);
                            ss.delete(start,i);
                            i = i - toDeleteS;
                            toDeleteS = 1;
                        }else{
                            toDeleteS ++;
                        }

                    }
                }
            }

            if(j >= 0){
                char chti = tt.charAt(j);
                if(chti != '#'){
                    builderT.append(chti);
                }else{
                    if(j > 0){
                        if(tt.charAt(j-1) != '#'){
                            int start = Math.max(j - toDeleteT, 0);
                            tt.delete(start,j);
                            j = j - toDeleteT;
                            toDeleteT = 1;
                        }else{
                            toDeleteT ++;
                        }
                    }
                }
            }

            if(builderS.length()>0 && builderT.length()>0){
                int minL = Math.min(builderS.length(), builderT.length());
                String substringS = builderS.substring(0, minL);
                String substringT = builderT.substring(0, minL);
                if(!substringS.equals(substringT)){
                    result = false;
                    break;
                }
            }
            i --;
            j --;
        }
        return result;
    }
}
