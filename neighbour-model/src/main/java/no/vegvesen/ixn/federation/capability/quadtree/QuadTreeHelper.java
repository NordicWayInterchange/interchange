package no.vegvesen.ixn.federation.capability.quadtree;

public class QuadTreeHelper {

    static char[] R = {'1','0','3','2'};
    static char[][]R_next = {null,R,null,R};

    static char[] L = {'1','0','3','2'};
    static char[][] L_next = {L,null,L,null};

    static char[] D = {'2','3','0','1'};
    static char[][] D_next = {null,null,D,D};

    static char[] U = {'2','3','0','1'};
    static char[][] U_next = {U,U,null,null};

    static char[] RU = {'3','2','1','0'};
    static char[][] RU_next = {U,RU,null,R};

    static char[] RD = {'3','2','1','0'};
    static char[][] RD_next = {null,R,D,RD};

    static char[] LD = {'3','2','1','0'};
    static char[][] LD_next = {L,null,LD,D};

    static char[] LU = {'3','2','1','0'};
    static char[][] LU_next = {LU,U,L,null};

    private static char[][] findNextArray(char[][] dir, int n){
        if(dir[n] == R) return R_next;
        else if(dir[n] == R) return R_next;
        else if(dir[n] == L) return L_next;
        else if(dir[n] == D) return D_next;
        else if(dir[n] == U) return U_next;
        else if(dir[n] == RU) return RU_next;
        else if(dir[n] == RD) return RD_next;
        else if(dir[n] == LD) return LD_next;
        else if(dir[n] == LU) return LU_next;
        else return null;
    }
    private static char[][] findNextArray(char[] dir){
        if(dir == R) return R_next;
        else if(dir == R) return R_next;
        else if(dir == L) return L_next;
        else if(dir == D) return D_next;
        else if(dir == U) return U_next;
        else if(dir == RU) return RU_next;
        else if(dir == RD) return RD_next;
        else if(dir == LD) return LD_next;
        else if(dir == LU) return LU_next;
        else return null;
    }

    public static StringBuilder getNeighbour(char[] direction, char[][] dir_next, int index, StringBuilder sb) {

        int n = ((int)sb.charAt(index))-48;
        sb.setCharAt(index,direction[n]);
        //System.out.println(n+"->"+direction[n]);

        if(dir_next[n] == null || index <= 0)
            return sb;
        else {
            return getNeighbour(dir_next[n], findNextArray(dir_next,n), --index, sb);
        }
    }

    public static String[] getNeighbours(String centerQT) {
        return new String[]{
                getNeighbour(LU, centerQT),
                getNeighbour(U, centerQT),
                getNeighbour(RU, centerQT),
                getNeighbour(L, centerQT),
                centerQT,
                getNeighbour(R, centerQT),
                getNeighbour(LD, centerQT),
                getNeighbour(D, centerQT),
                getNeighbour(RD, centerQT),
        };
    }

    public static String getNeighbour(char[] direction, String centerQT) {
        StringBuilder sb = new StringBuilder(centerQT);
        return getNeighbour(direction, findNextArray(direction), sb.length()-1, sb).toString();
    }

    public enum direction {
        LU,U,RU,L,R,LD,D,RD
    }

    public static String getNeighbour(QuadTreeHelper.direction dir, String centerQT) {
        char[] direction = null;
        switch (dir) {
            case LU:
                direction = LU;
                break;
            case U:
                direction = U;
                break;
            case RU:
                direction = RU;
                break;
            case L:
                direction = L;
                break;
            case R:
                direction = R;
                break;
            case LD:
                direction = LD;
                break;
            case D:
                direction = D;
                break;
            case RD:
                direction = RD;
                break;
        }
        StringBuilder sb = new StringBuilder(centerQT);
        return getNeighbour(direction, findNextArray(direction), sb.length()-1, sb).toString();
    }


    public static void main(String[] args) {

        String[] tiles = QuadTreeHelper.getNeighbours("1200031301320");
        for (String t : tiles) {
            System.out.println(t);
        }
        String tile = QuadTreeHelper.getNeighbour(QuadTreeHelper.direction.RD,"1200031301320");
        System.out.println(tile);

    }
}
