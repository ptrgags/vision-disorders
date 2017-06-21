# Vision Disorders

This app started as a project for my neuropsychology class. We had the freedom
to pick a research project to work on over the course of the term. As I am a
programmer interested in learning about computer graphics, I decided to do a
project about simulating neurological vision disorders. However, instead of
just generating images that simulate disorders like colorblindness or
hemianopia, I decided to use my Google Cardboard for a more immersive
experience.

## **DISCLAIMER**

This application is designed to show how disorienting vision disorders can be.
Some simulations can be a bit dizzying to look at. Please view with caution!
In particular, if you are sensitive to flashing lights or get motion sickness,
this app may not be for you.

## Dependencies:

In order to run this app, you need the following:

1. Android Studio or other IDE
2. Google Cardboard or other compatible
3. A Bluetooth controller is needed to navigate the menus. See
   [Buttons](#buttons) below for what buttons are needed.

This app uses the Google VR library

## Goals:

* Simulate a few different vision disorders
* Learn more about 3D graphics and OpenGL ES. Up until now, my experience with
  graphics was mostly limited to 2D only.
* Learn how the Google VR library works

## Usage

1. Install the app on an Android phone.
2. Connect to a Bluetooth Controller
3. Put the phone into a Google Cardboard
4. Explore the simulations. Each one has several variations

Simulation Controls:

When selecting a Bluetooth controller, try to use one that has at least one
button in each category

| Button(s) | Action |
|-----------|--------|
| D-Pad Right, B Button, R1 Button, Volume Up  | Go to next simulation |
| D-Pad Left, X Button, L1 Button, Volume Down | Go to previous simulation |
| D-Pad Up, Y Button, R2 Button, Cardboard Trigger | Next variation |
| D-Pad Down, A Button, L2 Button              | Previous variation |
| Start Button | Reset current simulation to the first variation |
| Select Button | toggle mode/variation indicators |

## Simulations

The program is composed of several simulations. Each one has several variations
to show normal vision in comparison to the disorder.

### Simulation 1: Color Blindness

The typical human has 3 types of cone cells, one each to view red, green and
blue light. However, some people have a cone that is not functioning normally
or is completely unusable. This condition is known as color blindness. Losing
a single cone results in a drastic reduction in the number of colors that can
be seen.

There are many different forms of color blindness or color deficiency. I chose
to implement a subset of them:

1. Normal color vision - this is for reference. The viewer stands in the middle
   of a RGB cube. Note where Red, Blue and Green are before viewing the other
   variations
2. Protanopia (Loss of red vision) - This is one of the two forms of red-green
   color blindness. Red and Green will be confused and will show up as yellow.
   Also note that even in the blue side of the cube, it is much harder to
   distinguish shades of color.
3. Deuteranopia (Loss of green vision) - This is the other form of red-green
   color blindness. It will look pretty similar to Protanopia.
4. Trianopia (Loss of blue vision) - When the blue cone is not working,
   blue and green are confused for a color in between - cyan.
5. Achromatopia (Loss of color vision) - When no cones are working, a person's
   vision relies on rod cells, which only view the world in grayscale.

### Simulation 2: Akinetopsia (Motion Blindness)

Akinetopsia is a disorder of the motion-processing areas of the brain
(V5 and V1). Someone suffering from this disorder sees the world in snapshots.

Variations:

1. Normal vision - A scene with some moving blocks to look at.
2. Akinetopsia - The frame rate is greatly reduced, simulating the stroboscopic
   vision of motion blindness.

### Simulation 3: Hemianopia (Partial loss of vision)

When someone is blind in one eye, this is usually due to some damage to one
eye or the other. If instead the neural pathways to the primary visual cortex
are damaged, the persion may lose vision in half of each eye, a condition
called Hemianopia.

Note that some of these simulations are not quite accurate due to the limited
field of view of the Google Cardboard.

Variations:

1. Normal vision - A scene with moving blocks to look at
2. Left Homonymous Hemianopia - Loss of vision in the left half of each eye.
3. Right Homonymous Hemianopia - Loss of vision in the right half of each eye.
4. Binasal Hemianopia - Loss of vision around the nose. **Note**: This is not
   as accurate as the others due to the limited field of view of the Google
   Cardboard.
5. Bitemporal Hemianopia - Loss of the half of the field of vision on the
   sides. **Note**: This is not as accurate due to the limited field of view
   of the Google Cardboard
6. Right Superior Quadrantanopia - Loss of vision in the upper-right quadrant
   of eye.
7. Left Inferior Quadrantanopia - Loss of vision in the bottom-left quadrant of
   each eye.

### Simulation 4: Tetrachromacy Analogy

Tetrachromacy is a condition where some women have a fourth cone cell. This
fourth cone cell detects light at wavelengths between red and green. This
additional cone allows a tetrachromat to see many more shades of color that
the rest of us cannot see.

Obviously, viewing the world like a tetrachromat sees it is impossible for
us trichromats, much like trying to visual shapes in 4 spacial dimensions.
However, it still can be explained by analogy. In this case, I chose to
describe it using color depth.

Imagine the days when we only had a few colors per red/green/blue channel.
Such a view of the world has color vision, but it only captures limited shades
of color. This is analogous to regular color vision with three cones.

Now think about graphics today. We have 256 colors per red/green/blue channel.
This is a huge increase in the number of colors we can now represent.
For example, between red and green, there are many, many shades of yellow and
orange. It just requires a sufficient color depth to view them. This is
analogous to tetrachromacy; a tetrachromat would be able to see even more
in-between shades that most of us will never see.

Variations:

1. Low color depth image - Pretend this is what a normal person can see
2. High color depth image - Notice the sudden increase of visible shades. This
   is analogous to tetrachromacy.
   
## References

Here are sources I referred to when making my project. Some were used for my in-class presentation.

* “Color Blindness Simulation.”, Internet Archive: Wayback Machine, 14 Oct.
  2008, web.archive.org/web/20081014161121/http://www.colorjack.com/labs/colormatrix/.
  Accessed 26 May 2017. 
* Deleniv, S. “The Mystery of Tetrachromacy: If 12% of Women Have Four Cone
  Types in Their Eyes, Why Do So Few of Them Actually See More Colours?” The
  Neurosphere, 16 Dec. 2015,
  http://theneurosphere.com/2015/12/17/the-mystery-of-tetrachromacy-if-12-of-women-have-four-cone-types-in-their-eyes-why-do-so-few-of-them-actually-see-more-colours/.
  Accessed 26 May 2017.
* Flück, Daniel. “Colorblind Colors of Confusion” Colblindor, 19 Jan. 2009,
  http://www.color-blindness.com/2009/01/19/colorblind-colors-of-confusion/.
  Accessed 26 May 2017.
* Flück, Daniel. “Colorblind Population” Colblindor, 28 Apr. 2006,
  http://www.color-blindness.com/2006/04/28/colorblind-population/.
  Accessed 26 May 2017.
* Flück, Daniel. “Confusion Lines of the CIE 1931 Color Space.” Colblindor,
  23 Jan. 2007,
  http://www.color-blindness.com/2007/01/23/confusion-lines-of-the-cie-1931-color-space/.
  Accessed 26 May 2017.
* Flück, Daniel. “Types of Color Blindness” Colblindor,
  http://www.color-blindness.com/types-of-color-blindness/
  Accessed 26 May 2017.
* “Hemianopia.” Help For Vision Loss, Noravision, 2017,
  http://www.helpforvisionloss.com/group-holder/2011-06-18-17-13-45/hemianopia.html.
  Accessed 26 May 2017.
* Moore, Jesse. “Physiology.” Akinetopsia,
  http://mooreperceptionproject.weebly.com/physiology.html.
  Accessed 26 May 2017.
* Moore, Jesse. “Symptoms.” Akinetopsia,
  http://mooreperceptionproject.weebly.com/symptoms.html. 
  Accessed 26 May 2017.
