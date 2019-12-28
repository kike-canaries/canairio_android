package hpsaturn.pollutionreporter.api;

import java.util.List;

/**
 * Created by Antonio Vanegas @hpsaturn on 12/28/19.
 */
public class AqicnData {

    public int idx;

    public int aqi;

    public AqicnTime time;

    public AqicnCity city;

    public List<AqicnAttributions> attributions;

    public AqicnIaqi iaqi;
}
