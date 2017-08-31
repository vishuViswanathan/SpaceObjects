package GeneralElements.Display;

import GeneralElements.Item;
import GeneralElements.ItemInterface;

import javax.media.j3d.RenderingAttributes;

/**
 * Created by M Viswanathan on 09 Mar 2017
 */
public interface AttributeSetter {
    public ItemInterface getItem();

    public void setRenderingAttribute(RenderingAttributes renderingAttribute);
}