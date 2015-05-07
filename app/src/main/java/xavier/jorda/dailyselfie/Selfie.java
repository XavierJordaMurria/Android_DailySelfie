package xavier.jorda.dailyselfie;

/**
 * Created by xj on 28/03/15.
 */

import android.graphics.Bitmap;

public class Selfie
{

    private String selfieName;
    private Bitmap selfieThumb;
    private String selfiePath;

    public Selfie(String name, String fullFilePath, Bitmap thumbnail)
    {
        this.selfieName = name;
        this.selfiePath = fullFilePath;
        this.selfieThumb = thumbnail;
    }

    public String getSelfieName()
    {
        return selfieName;
    }

    public String getSelfiePath()
    {
        return selfiePath;
    }

    public Bitmap getSelfieThumb()
    {
        return selfieThumb;
    }
}