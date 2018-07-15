package hpsaturn.pollutionreporter.view;

/**
 * Created by Antonio Vanegas @hpsaturn on 10/25/15.
 */
public interface ItemTouchHelperAdapter {

    void onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);

    void onItemAction(int position, int action);

}
