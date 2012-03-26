package eu.tpmusielak.securephoto.camera;

import android.app.AlertDialog;
import android.content.DialogInterface;
import eu.tpmusielak.securephoto.R;
import eu.tpmusielak.securephoto.verification.*;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 26/03/12
 * Time: 02:38
 */
public class SCVerifierManager extends BasicVerifierManager implements VerifierBinder {

    private TakeImage activity;

    protected List<VerifierWrapper> verifierWrappers;

    @SuppressWarnings("unchecked")
    public SCVerifierManager(TakeImage activity) {
        super();
        this.activity = activity;
        verifierWrappers = new LinkedList<VerifierWrapper>();


        new GenericVerifierWrapper(new DummyVerifier()).register(this);
        new GenericVerifierWrapper(new DummyVerifier(2000)).register(this);
        new GenericVerifierWrapper(new DummyVerifier(4000)).register(this);
        new GenericVerifierWrapper(new DummyVerifier(10000)).register(this);

    }

    @Override
    public void register(VerifierWrapper wrapper) {
        verifierWrappers.add(wrapper);
        wrapper.setActivity(activity);
        wrapper.setPluginsPane(activity.pluginsPane);
        Verifier v = wrapper.getVerifier();
        super.register(v);
    }

    public void showVerificationFactors() {
        List<CharSequence> verificationFactorNames = new LinkedList<CharSequence>();

        for (VerifierWrapper v : verifierWrappers) {
            verificationFactorNames.add(v.getName());
        }

        CharSequence[] items = new CharSequence[verificationFactorNames.size()];

        verificationFactorNames.toArray(items);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.pick_vfactor);

        boolean[] enabledV = getVerifierStatusArray();

        builder.setMultiChoiceItems(items, enabledV, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
            }
        });

        setVerifierStatusArray(enabledV);

        AlertDialog alert = builder.create();
        alert.show();

    }


}
