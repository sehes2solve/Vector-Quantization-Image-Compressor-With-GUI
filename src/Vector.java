import java.util.Arrays;

public class Vector
{
    double[][] pixels;
    CodeBookVector cbv;
    public Vector(int w,int h)
    {
        pixels = new double[w][h];
    }
    public double get_distance(Vector v)
    {
        if(v.pixels.length != pixels.length || v.pixels[0].length != pixels[0].length)
            return -1;
        double dist = 0;
        for(int i = 0;i < pixels.length;i++)
            for(int j = 0;j < v.pixels[0].length;j++)
                dist += Math.abs(pixels[i][j] - v.pixels[i][j]);
        return dist;
    }
}
