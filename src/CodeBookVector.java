import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class CodeBookVector extends Vector
{
    public static boolean distribute_vectors(ArrayList<Vector> vectors,ArrayList<CodeBookVector> code_book_vectors,boolean is_link_vec_to_cbv)
    {
        int min_idx = -1;
        double min_dist, dist;
        boolean is_vector_jumped = false;
        HashMap<CodeBookVector,ArrayList<Vector>> updated_vectors = new HashMap<>();
        for(Vector v : vectors)
        {
            /** get nearest code book vector **/
            min_dist = 1e9;
            for(int i = 0;i < code_book_vectors.size();i++)
            {
                dist = code_book_vectors.get(i).get_distance(v);
                if(dist < min_dist)
                {
                    min_dist = dist;
                    min_idx = i;
                }
            }
            /** add vector to new group of vectors for the nearest code book vector **/
            if(updated_vectors.get(code_book_vectors.get(min_idx)) == null)
                updated_vectors.put(code_book_vectors.get(min_idx),new ArrayList<>());
            updated_vectors.get(code_book_vectors.get(min_idx)).add(v);
            if(is_link_vec_to_cbv)
                v.cbv = code_book_vectors.get(min_idx);
        }
        /** compare new group of vectors for each code book with old group of vectors **/
        for(CodeBookVector cbv : code_book_vectors)
        {
            if(updated_vectors.get(cbv) == null)
            {
                cbv.vectors.clear();
                continue;
            }
            if(!    (cbv.vectors.size() == updated_vectors.get(cbv).size()
                    && updated_vectors.get(cbv).containsAll(cbv.vectors)))
            {
                is_vector_jumped = true;
                cbv.vectors = updated_vectors.get(cbv);
            }
        }
        return is_vector_jumped;
    }
    String code;
    ArrayList<Vector> vectors;
    public CodeBookVector(int w,int h)
    {
        super(w,h);
        vectors = new ArrayList<>();
    }
    public CodeBookVector(int w,int h,int pixel)
    {
        super(w,h);
        set_pixels(pixel);
        vectors = new ArrayList<>();
    }
    public void set_pixels(int pixel)
    {
        for(double[] px_col : pixels)
            Arrays.fill(px_col,pixel);
    }
    public void divide(int divisor)
    {
        if(divisor == 0)
        {
            set_pixels(0);
            return;
        }
        for(double[] px_col:pixels)
            for(int j = 0;j < px_col.length;j++)
                px_col[j] /= divisor;
    }
    public void set_avg()
    {
        set_pixels(0);
        for (Vector v: vectors)
        {
            for(int i = 0;i < pixels.length;i++)
                for(int j = 0;j < pixels[i].length;j++)
                    pixels[i][j] += v.pixels[i][j];
        }
        divide(vectors.size());
    }
    public CodeBookVector split(CodeBookVector right_cb_vec)
    {
        CodeBookVector left_cb_vec = new CodeBookVector(pixels.length,pixels[0].length);
        for(int i = 0;i < pixels.length;i++)
            for(int j = 0;j < pixels[i].length;j++)
            {
                left_cb_vec.pixels[i][j] = Math.floor(pixels[i][j]);
                right_cb_vec.pixels[i][j] = Math.ceil(pixels[i][j]);
                if(left_cb_vec.pixels[i][j] == right_cb_vec.pixels[i][j])
                {
                    left_cb_vec.pixels[i][j]--;
                    right_cb_vec.pixels[i][j]++;
                }
            }
        return left_cb_vec;
    }
}
