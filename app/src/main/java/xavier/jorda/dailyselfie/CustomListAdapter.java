package xavier.jorda.dailyselfie;

/**
 * Created by xj on 28/03/15.
 */

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class CustomListAdapter extends ArrayAdapter<Selfie>
{

    private final Context context;

    public CustomListAdapter(Context context, int resource, int textViewResource, List<Selfie> objects)
    {
        super(context, resource, textViewResource, objects);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Selfie selfie = getItem(position);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        final ViewHolder viewHolder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.selfie_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.textView = (TextView) convertView.findViewById(R.id.dailyselfieText);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.dailyselfieImage);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (selfie != null) {
            viewHolder.textView.setText(getReadableSelfieName(selfie.getSelfieName()));
            viewHolder.imageView.setImageBitmap(selfie.getSelfieThumb());
        }

        return convertView;
    }

    protected static String getReadableSelfieName(String selfieName)
    {
        String[] split = selfieName.split("_");
        String date = split[1];
        String time = split[2];

        String[] splitTime = time.split("-");

        String dateTime = date +"_"+ splitTime[0]+":"+splitTime[1]+":"+splitTime[2];

        return "Selfie_"+dateTime;
    }

    private class ViewHolder
    {
        TextView textView;
        ImageView imageView;
    }
}

