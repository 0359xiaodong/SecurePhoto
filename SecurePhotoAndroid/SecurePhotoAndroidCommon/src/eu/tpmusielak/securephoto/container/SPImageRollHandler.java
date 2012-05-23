package eu.tpmusielak.securephoto.container;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import eu.tpmusielak.securephoto.R;
import eu.tpmusielak.securephoto.tools.FileHandling;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 25/03/12
 * Time: 17:13
 */
public class SPImageRollHandler implements SPFileHandler {

    private VerifierProvider verifierProvider;
    private File rollFile;

    public SPImageRollHandler(VerifierProvider provider) {
        this.verifierProvider = provider;
    }

    @Override
    public File saveFile(byte[] bytes) {
        //TODO: Check auto-generated code
        return null;
    }

    @Override
    public void onInitialize(final Context context) {
        final File[] rolls = FileHandling.getFiles(".spr");
        final String[] names = new String[rolls.length];

        for (int i = 0; i < rolls.length; i++) {
            names[i] = rolls[i].getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.select_roll);
        builder.setItems(names, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                rollFile = rolls[i];
            }
        });
        builder.setCancelable(true);

        AlertDialog alert = builder.create();
        alert.show();
    }


}
