package eu.tpmusielak.securephoto.verification;

import android.content.Context;
import android.graphics.drawable.Drawable;
import eu.tpmusielak.securephoto.R;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 26/03/12
 * Time: 01:22
 */
public class GenericVFWrapper implements VerificationFactorWrapper {
    private VerificationFactor verificationFactor;
    private Context context;

    public GenericVFWrapper(VerificationFactor verificationFactor, Context context) {
        this.verificationFactor = verificationFactor;
        this.context = context;
    }

    public VerificationFactor getVerificationFactor() {
        return verificationFactor;
    }

    @Override
    public String getName() {
        return verificationFactor.toString();
    }

    @Override
    public Drawable getDrawable() {
        return context.getResources().getDrawable(R.drawable.ic_menu_camera);
    }
}
