package io.atomofiron.tolmach;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import io.atomofiron.tolmach.fragments.AboutFragment;
import io.atomofiron.tolmach.fragments.MainFragment;
import io.atomofiron.tolmach.fragments.PrefFragment;

public class MainActivity extends AppCompatActivity {
	private FragmentManager fragmentManager = null;
	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		actionBar = getSupportActionBar();

		fragmentManager = getSupportFragmentManager();
		if (fragmentManager.findFragmentById(R.id.container) == null) {
			fragmentManager.beginTransaction()
					.replace(R.id.container, new MainFragment())
					.commitAllowingStateLoss();
		}
	}

	private void addFragment(Fragment fragment) {
		actionBar.setDisplayHomeAsUpEnabled(true);

		fragmentManager.beginTransaction()
				.addToBackStack(null)
				.replace(R.id.container, fragment)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.commitAllowingStateLoss();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.about:
				addFragment(new AboutFragment());
				break;
			case R.id.preferences:
				addFragment(new PrefFragment());
				break;
			case android.R.id.home:
				onBackPressed();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

		if (fragmentManager.getBackStackEntryCount() == 0)
			actionBar.setDisplayHomeAsUpEnabled(false);
	}

	@TargetApi(Build.VERSION_CODES.M)
	public void showPermissionDialog() {
		new AlertDialog.Builder(this)
				.setCancelable(false)
				.setMessage(R.string.permission_request)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						requestPermissions(new String[]{ Manifest.permission.RECORD_AUDIO }, I.PERMISSION_REQUEST_CODE);
					}
				}).setNegativeButton(R.string.quit, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}).create().show();
	}


	@TargetApi(Build.VERSION_CODES.M)
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
			if (shouldShowRequestPermissionRationale(permissions[0]))
				showPermissionDialog();
			else
				Snackbar.make(findViewById(R.id.container), R.string.no_mic_permission, Snackbar.LENGTH_SHORT).show();
		}
	}
}
