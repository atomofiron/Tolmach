package io.atomofiron.tolmach;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import io.atomofiron.tolmach.fragments.AboutFragment;
import io.atomofiron.tolmach.fragments.MainFragment;

public class MainActivity extends AppCompatActivity {
	private FragmentManager fragmentManager = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		fragmentManager = getSupportFragmentManager();
		if (fragmentManager.findFragmentById(R.id.container) == null)
			fragmentManager.beginTransaction()
					.replace(R.id.container, new MainFragment())
					.commitAllowingStateLoss();
	}

	private void addFragment(Fragment fragment) {
		fragmentManager.beginTransaction()
				.addToBackStack(null)
				.replace(R.id.container, fragment)
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
		}
		return super.onOptionsItemSelected(item);
	}
}
