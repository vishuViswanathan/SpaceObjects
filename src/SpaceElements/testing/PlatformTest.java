package SpaceElements.testing;

/**********************************************************
  Copyright (C) 2001 	Daniel Selman

  First distributed with the book "Java 3D Programming"
  by Daniel Selman and published by Manning Publications.
  http://manning.com/selman

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation, version 2.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  The license can be found on the WWW at:
  http://www.fsf.org/copyleft/gpl.html

  Or by writing to:
  Free Software Foundation, Inc.,
  59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

  Authors can be contacted at:
   Daniel Selman: daniel@selman.org

  If you make changes you think others would like, please
  contact one of the authors or someone at the
  www.j3d.org web site.
**************************************************************/


import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;

import javax.media.j3d.*;
import javax.vecmath.*;

import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.behaviors.keyboard.KeyNavigatorBehavior;

/**
* This example illustrates some of the features of the SimpleUniverse
* class. It creates 3 views into a scengraph and associates a ViewerAvatar
* and PlatformGeometry with each Viewer object.<p>
* The example creates a very simple navigation environment where
* each Viewer of the environment can navigate using the keyboard
* and see the other viewer.
*/
public class PlatformTest extends Applet implements ActionListener
{
	// size of each Canvas3D
	static final int					m_kWidth = 256;
	static final int					m_kHeight = 256;

	// table used to map the name of a Viewer to its KeyNavigatorBehavior
	Hashtable							m_KeyHashtable = null;
	BoundingSphere						m_Bounds = null;


	public PlatformTest( )
	{
		m_KeyHashtable = new Hashtable( );
		m_Bounds = new BoundingSphere( new Point3d( 0, 0, 0 ), 100 );
		// get the graphics configuration for the graphics device
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration( );

		// create the first canvas, this is the top-down view
		Canvas3D c = new Canvas3D( config );
		c.setSize( m_kWidth, m_kHeight );
		add( c );

		// create the second canvas, this is used for "Jim's" Viewer
		Canvas3D c2 = new Canvas3D( config );
		c2.setSize( m_kWidth, m_kHeight );
		add( c2 );

		// create the third canvas, this is used for "Dan's" Viewer
		Canvas3D c3 = new Canvas3D( config );
		c3.setSize( m_kWidth, m_kHeight );
		add( c3 );

		// Create the simple environment
		BranchGroup scene = createSceneGraph( );

		// create the first Viewer, this is a static top-down view
		// create a ViewingPlatform with 2 TransformGroups above the ViewPlatform
		ViewingPlatform vp = new ViewingPlatform( 2 );

		// create the Viewer and attach to the first canvas
		Viewer viewer = new Viewer( c );

		// rotate and position the first Viewer above the environment
		Transform3D t3d = new Transform3D( );
		t3d.rotX( Math.PI / 2.0 );
		t3d.setTranslation( new Vector3d( 0, 0, -40 ) );
		t3d.invert( );

		MultiTransformGroup mtg = vp.getMultiTransformGroup( );
		mtg.getTransformGroup( 0 ).setTransform( t3d );

		// create a SimpleUniverse from the ViewingPlatform and Viewer
		SimpleUniverse u = new SimpleUniverse( vp, viewer );

		// add the geometry to the scenegraph
		u.addBranchGraph( scene );

		// add two more Viewers to the scenegraph
		u.getLocale( ).addBranchGraph( createViewer( c2, "Jim", new Color3f( 0.1f, 1.0f, 1.0f ), -5, 8 ) );
		u.getLocale( ).addBranchGraph( createViewer( c3, "Dan", new Color3f( 1.0f, 0.1f, 0.1f ), 2, -8 ) );

	}

	ViewingPlatform createViewer( Canvas3D c, String szName, Color3f objColor, double x, double z )
	{
		// create a Viewer and attach to its canvas
		// a Canvas3D can only be attached to a single Viewer
		Viewer viewer2 = new Viewer( c );

		// create a ViewingPlatform with 1 TransformGroups above the ViewPlatform
		ViewingPlatform vp2 = new ViewingPlatform( 1 );

		// create and assign the PlatformGeometry to the Viewer
		vp2.setPlatformGeometry( createPlatformGeometry( szName ) );

		// create and assign the ViewerAvatar to the Viewer
		viewer2.setAvatar( createViewerAvatar( szName, objColor ) );

		// set the initial position for the Viewer
		Transform3D t3d = new Transform3D( );
		t3d.setTranslation( new Vector3d( x, 0, z ) );
		vp2.getViewPlatformTransform( ).setTransform( t3d );

		// set capabilities on the TransformGroup so that the KeyNavigatorBehavior
		// can modify the Viewer's position
		vp2.getViewPlatformTransform( ).setCapability( TransformGroup.ALLOW_TRANSFORM_WRITE );
		vp2.getViewPlatformTransform( ).setCapability( TransformGroup.ALLOW_TRANSFORM_READ );

		// attach a navigation behavior to the position of the viewer
		KeyNavigatorBehavior key = new KeyNavigatorBehavior( vp2.getViewPlatformTransform( ) );
		key.setSchedulingBounds( m_Bounds );
		key.setEnable( false );

		// add the KeyNavigatorBehavior to the ViewingPlatform
		vp2.addChild( key );

		// set the ViewingPlatform for the Viewer
		viewer2.setViewingPlatform( vp2 );

		// associate the name of the Viewer with its KeyNavigatorBehavior
		m_KeyHashtable.put( szName, key );

		// create a button to switch the Viewer ON.
		Button button = new Button( szName );
		button.addActionListener( this );
		add( button );

		return vp2;
	}

	// create a tiled environment from -12 to +12. The environment
	// is created from a QuadArray. The environment is surrounded by a ColorCube
	// "wall" that is 2 units high (from Z = -1 to Z = 1).
	public BranchGroup createSceneGraph( )
	{
		final int LAND_WIDTH = 12;
		final float LAND_HEIGHT = -1.0f;
		final int LAND_LENGTH = 12;
		final int nTileSize = 2;

		// calculate how many vertices we need to store all the "tiles"
		// that compose the QuadArray.
		final int nNumTiles = ((LAND_LENGTH/nTileSize) * 2 ) * ((LAND_WIDTH/nTileSize) * 2 );
		final int nVertexCount = 4 * nNumTiles;
		Point3f[] coordArray = new Point3f[nVertexCount];
		Point2f[] texCoordArray = new Point2f[nVertexCount];

		// create an Appearance and load a texture
		Appearance app = new Appearance( );
		Texture tex = new TextureLoader( "land.jpg", this ).getTexture( );
		app.setTexture( tex );

		// create the parent BranchGroup
		BranchGroup bg = new BranchGroup( );

		int nItem = 0;

		// loop over all the tiles in the environment
		for( int x = -LAND_WIDTH; x <= LAND_WIDTH; x+=nTileSize )
		{
			for( int z = -LAND_LENGTH; z <= LAND_LENGTH; z+=nTileSize )
			{
				// if we are on the border of the environment create a
				// TransformGroup to position a ColorCube to create a "wall"
				if( x == -LAND_WIDTH || x == LAND_WIDTH || z == -LAND_LENGTH || z == LAND_LENGTH )
				{
					TransformGroup tg = new TransformGroup( );
					Transform3D t3d = new Transform3D( );
					t3d.setTranslation( new Vector3d( x, 0, z ) );
					tg.setTransform( t3d );
					tg.addChild( new ColorCube( nTileSize/2 ) );
					bg.addChild( tg );
				}

				// if we are not on the last row or column create a "tile"
				// and add to the QuadArray. Use CCW winding and assign texture
				// coordinates.
				if( z < LAND_LENGTH && x < LAND_WIDTH )
				{
					coordArray[nItem] = new Point3f( x, LAND_HEIGHT, z );
					texCoordArray[nItem++] = new Point2f( 0, 0 );
					coordArray[nItem] = new Point3f( x, LAND_HEIGHT, z + nTileSize );
					texCoordArray[nItem++] = new Point2f( 1, 0 );
					coordArray[nItem] = new Point3f( x + nTileSize, LAND_HEIGHT, z + nTileSize );
					texCoordArray[nItem++] = new Point2f( 1, 1 );
					coordArray[nItem] = new Point3f( x + nTileSize, LAND_HEIGHT, z );
					texCoordArray[nItem++] = new Point2f( 0, 1 );
				}
			}
		}

		// create a GeometryInfo and generate Normal vectors
		// for the QuadArray that was populated.
		GeometryInfo gi = new GeometryInfo( GeometryInfo.QUAD_ARRAY );

		gi.setCoordinates( coordArray );
		gi.setTextureCoordinates( texCoordArray );

		NormalGenerator normalGenerator = new NormalGenerator( );
		normalGenerator.generateNormals( gi );

		// wrap the GeometryArray in a Shape3D
		Shape3D shape = new Shape3D( gi.getGeometryArray( ), app );

		// add the Shape3D to the parent BranchGroup
		bg.addChild( shape );

		// create some lights for the scene
		Color3f lColor1 = new Color3f( 0.7f, 0.7f, 0.7f );
		Vector3f lDir1  = new Vector3f( -1.0f, -1.0f, -1.0f );
		Color3f alColor = new Color3f( 0.2f, 0.2f, 0.2f );

		AmbientLight aLgt = new AmbientLight( alColor );
		aLgt.setInfluencingBounds( m_Bounds );
		DirectionalLight lgt1 = new DirectionalLight( lColor1, lDir1 );
		lgt1.setInfluencingBounds( m_Bounds );

		// add the lights to the parent BranchGroup
		bg.addChild( aLgt );
		bg.addChild( lgt1 );

		// create a light gray background
		Background back = new Background( new Color3f( 0.9f, 0.9f, 0.9f ) );
		back.setApplicationBounds( m_Bounds );
		bg.addChild( back );

		// compile the whole scene
		//bg.compile();

		return bg;
	}

	// creates and positions a simple Cone to represent the Viewer.
	// The Cone is aligned and scaled such that it is similar to a
	// 3D "turtle".... Aaah good old Logo.
	ViewerAvatar createViewerAvatar( String szText, Color3f objColor )
	{
		ViewerAvatar viewerAvatar = new ViewerAvatar( );

		// rotate the Cone so that it is lying down and
		// points sharp-end towards the Viewer's field of view.
		TransformGroup tg = new TransformGroup( );
		Transform3D t3d = new Transform3D( );
		t3d.setEuler( new Vector3d( Math.PI / 2.0, Math.PI, 0 ) );
		tg.setTransform( t3d );

		// create appearance and material for the Cone
		Appearance app = new Appearance( );
		Color3f black = new Color3f( 0.4f, 0.2f, 0.1f );
		app.setMaterial( new Material( objColor, black, objColor, black, 90.0f ) );

		// create the Primitive and add to the parent BranchGroup
		tg.addChild( new Cone( 1, 3, Primitive.GENERATE_NORMALS, app ) );
		viewerAvatar.addChild( tg );

		return viewerAvatar;
	}

	// create a simple Raster text label used to help
	// identify the viewer.
	PlatformGeometry createPlatformGeometry( String szText )
	{
		PlatformGeometry pg = new PlatformGeometry( );
		pg.addChild( createLabel( szText, 0f, 2f, 0f ) );

		return pg;
	}

	// creates a simple Raster text label (similar to Text2D)
	private Shape3D createLabel( String szText, float x, float y, float z )
	{
		BufferedImage bufferedImage = new BufferedImage( 25, 14, BufferedImage.TYPE_INT_RGB );
		Graphics g = bufferedImage.getGraphics( );
		g.setColor( Color.white );
		g.drawString( szText, 2, 12 );

		ImageComponent2D imageComponent2D = new ImageComponent2D( ImageComponent2D.FORMAT_RGB, bufferedImage );

		// create the Raster for the image
		javax.media.j3d.Raster renderRaster = new javax.media.j3d.Raster ( new Point3f( x, y, z ),
			javax.media.j3d.Raster.RASTER_COLOR,
			0, 0,
			bufferedImage.getWidth( ),
			bufferedImage.getHeight( ),
			imageComponent2D,
			null );

		return new Shape3D( renderRaster );
	}


	// Enables the KeyNavigatorBehavior associated with the
	// AWT button that was pressed for the Viewer. Disables all other
	// KeyNavigatorBehaviors for non-active Viewers.
	public void actionPerformed( ActionEvent event )
	{
		KeyNavigatorBehavior key = (KeyNavigatorBehavior) m_KeyHashtable.get( event.getActionCommand( ) );
		Object[] keysArray = m_KeyHashtable.values( ).toArray( );

		for( int n = 0; n < keysArray.length; n++ )
		{
			KeyNavigatorBehavior keyAtIndex = (KeyNavigatorBehavior) keysArray[n];
			keyAtIndex.setEnable( keyAtIndex == key );

			if( keyAtIndex == key )
				System.out.println( "Enabled: " + event.getActionCommand( ) );
		}
	}

	public static void main( String[] args )
	{
		new MainFrame( new PlatformTest( ), (int) (m_kWidth * 3.5), (int) (m_kHeight * 1.1) );
	}
}
