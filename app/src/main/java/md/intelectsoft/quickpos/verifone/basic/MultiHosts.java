package md.intelectsoft.quickpos.verifone.basic;

import android.util.Log;

import java.util.Vector;

/**
 * Created by Simon on 2019/1/23.
 */

public class MultiHosts {
    private static final String TAG = "EMVDemo-MultiHosts";

    private static Vector<HostInformation> hostInformations;

    public MultiHosts(){
        hostInformations = new Vector<>();
    }

    public int append( HostInformation hostInformation){
        hostInformation.index = hostInformations.size();
        if( hostInformations.add(hostInformation) ) {
            return hostInformations.size()-1;
        } else {
            return -1;
        }
    }

    public int update( int index, HostInformation hostInformation ){
        hostInformation.index = index;
        hostInformations.set(index, hostInformation);
        return index;
    }

    public HostInformation get( int index ){
        return hostInformations.get(index);
    }

    public void removeAll (){
        hostInformations = null;
    }

    public void remove( int index ){
        if( index < hostInformations.size() ){
            hostInformations.set(index, null);
        }
    }

    public HostInformation getHost(int index, String cardBin, String aid){
        Log.d(TAG, "call getHost, index=" + index + " , cardBin=" + cardBin +", aid="+ aid );
        if( null == hostInformations ){
            return null;
        }
        int count = hostInformations.size();
        if( index >= 0 && index < count ){
            return hostInformations.get(index);
        }
        HostInformation hostInformation = null;
        --count;
        for( ; count >= 0; count-- ){
            hostInformation = hostInformations.get(count);
            Log.d(TAG, "read information:" + hostInformation.description );
            if( null == hostInformation ){
                continue;
            }
            if ( hostInformation.checkValid(cardBin,aid) ) {
                Log.e(TAG, "hit");
                break;
            }
        }
        Log.e(TAG, "before return");
        return hostInformation;
    }
}
