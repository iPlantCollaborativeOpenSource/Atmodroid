//package org.iplantcollaborative.atmo.mobile.bird;
//
//import java.util.ArrayList;
//
//import android.app.ListActivity;
//import android.app.ProgressDialog;
//import android.content.Context;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//public class InstanceListView extends ListActivity{
//   
//    private ProgressDialog m_ProgressDialog = null;
//    private ArrayList<AtmoInstance> m_orders = null;
//    private OrderAdapter m_adapter;
//    private Runnable viewOrders;
//    private AtmoAPI myatmo;
//    
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.list);
//        myatmo = new AtmoAPI("https://atmo-beta.iplantcollaborative.org:443/auth");
//        myatmo.authenticate("esteve", "s%#v#!");
//        m_orders = new ArrayList<AtmoInstance>();
//        this.m_adapter = new OrderAdapter(this, R.layout.row, m_orders);
//        setListAdapter(this.m_adapter);
//       
//        viewOrders = new Runnable(){
//            @Override
//        	public void run() {
//            	super();
//                getOrders();
//            }
//        };
//        Thread thread =  new Thread(null, viewOrders, "AtmoDroidBackground");
//        thread.start();
//        m_ProgressDialog = ProgressDialog.show(InstanceListView.this,    
//              "Downloading Data", "Retrieving Instances From Atmo..", true);
//    }
//    private Runnable returnRes = new Runnable() {
//    	//A Smaller thread to be run on the UI after slow method completes
//        @Override
//        public void run() {
//        	//If 1+ instances... Notify datasetchange... Add each instance to adapter.
//            if(m_orders != null && m_orders.size() > 0){
//                m_adapter.notifyDataSetChanged();
//                for(int i=0;i<m_orders.size();i++)
//                m_adapter.add(m_orders.get(i));
//            }
//            m_ProgressDialog.dismiss();
//            m_adapter.notifyDataSetChanged();
//        }
//    };
//    private void getOrders(){
//          try{
//        	  // Get AtmoInstances from server & add to array
//              m_orders = new ArrayList<AtmoInstance>(myatmo.getInstances().values());
//              Log.i("ARRAY", ""+ m_orders.size());
//            } catch (Exception e) {
//              Log.e("BACKGROUND_PROC", e.getMessage());
//            }
//            //Call fast UI thread after slow part is done..
//            runOnUiThread(returnRes);
//        }
//
//    //Custom ListActivity Adapter
//    private class OrderAdapter extends ArrayAdapter<AtmoInstance> {
//
//        private ArrayList<AtmoInstance> items;
//
//        public OrderAdapter(Context context, int textViewResourceId, ArrayList<AtmoInstance> items) {
//                super(context, textViewResourceId, items);
//                this.items = items;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//        		//Each time Adapter refreshes..
//                View v = convertView;
//                if (v == null) {
//                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                    v = vi.inflate(R.layout.row, null);
//                }
//                //Find the Instance corresponding to the position in the adapter
//                AtmoInstance o = items.get(position);
//                if (o != null) {
//                        TextView tt = (TextView) v.findViewById(R.id.toptext);
//                        TextView bt = (TextView) v.findViewById(R.id.bottomtext);
//                        ImageView iv = (ImageView) v.findViewById(R.id.icon2);
//                        if (tt != null) {
//                              tt.setText("Name: "+o.getName());                            
//                        }
//                        if(bt != null){
//                              bt.setText("Status: "+ o.getInstance_state());
//                        }
//                        if(iv != null) {
//                        	String status = o.getInstance_state();
//                        	if(status.equalsIgnoreCase("pending"))
//                        		iv.setImageResource(R.drawable.pending);
//                        	else if(status.equalsIgnoreCase("running"))
//                        		iv.setImageResource(R.drawable.running);
//                        	else
//                        		iv.setImageResource(R.drawable.shutting_down);
//                        }
//                        //Add Name, Status and Image to row before returning view.
//                }
//                return v;
//        }
//}
//}