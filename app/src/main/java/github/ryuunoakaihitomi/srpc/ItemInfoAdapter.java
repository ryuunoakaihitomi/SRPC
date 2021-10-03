package github.ryuunoakaihitomi.srpc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ItemInfoAdapter extends ArrayAdapter<ItemInfo> {

    private static final int RESOURCE_ID = R.layout.item_app_list;

    public ItemInfoAdapter(Context context, List<ItemInfo> objects) {
        super(context, RESOURCE_ID, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Context c = getContext();
        if (convertView == null) {
            convertView = LayoutInflater.from(c).inflate(RESOURCE_ID, parent, false);
            holder = new ViewHolder();
            holder.iconImage = convertView.findViewById(R.id.img_app_icon);
            holder.appLabelText = convertView.findViewById(R.id.text_app_label);
            holder.permStateText = convertView.findViewById(R.id.text_perm_state);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ItemInfo info = getItem(position);
        holder.iconImage.setImageDrawable(info.icon);
        holder.appLabelText.setText(info.label);
        TextView permStateText = holder.permStateText;
        if (info.rState == 0 && info.wState == 0) {
            permStateText.setVisibility(View.GONE);
        } else {
            permStateText.setVisibility(View.VISIBLE);
            permStateText.setText(String.format("[%s: %s, %s: %s]", c.getString(R.string.read), getStateText(info.rState), c.getString(R.string.write), getStateText(info.wState)));
        }
        return convertView;
    }

    private static class ViewHolder {
        ImageView iconImage;
        TextView appLabelText, permStateText;
    }

    private String getStateText(int state) {
        return getContext().getResources().getStringArray(R.array.perm_state)[state];
    }
}
