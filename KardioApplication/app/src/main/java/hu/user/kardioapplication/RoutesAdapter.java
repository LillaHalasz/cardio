package hu.user.kardioapplication;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by User on 2016.04.27..
 */
public class RoutesAdapter extends RecyclerView.Adapter<RoutesAdapter.ViewHolder>
{


    private List<Route> mRoute;

    public RoutesAdapter(List<Route> routes)
    {
        mRoute = routes;
    }

    @Override
    public RoutesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View routeView = inflater.inflate(R.layout.route_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(routeView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        holder.routeName.setText(mRoute.get(position).name);
        holder.routeDesc.setText(mRoute.get(position).description);
        holder.setClickListener(new ItemClickListener()
        {
            @Override
            public void onClick(View view, int position)
            {
                Log.i("OnClick", "" + position );

            }
        });


    }

    @Override
    public int getItemCount()
    {
        return mRoute.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        public TextView routeName;
        public TextView routeDesc;
        private ItemClickListener clickListener;


        public ViewHolder(View itemView)
        {
            super(itemView);

            routeName = (TextView) itemView.findViewById(R.id.tvRouteName);
            routeDesc = (TextView) itemView.findViewById(R.id.tvRouteDesc);
            itemView.setOnClickListener(this);
        }
        public void setClickListener(ItemClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }
        @Override
        public void onClick(View view) {
            clickListener.onClick(view, getPosition());
        }


    }


}
