# CS 330: Computational Graphics and Visualization

Final project for CS 330 at Southern New Hampshire University: a 3D recreation of my own desk, built in C++ and OpenGL from the five basic primitives available in the course mesh library.

## Contents

- `7-1 Final Project.zip` — the full Visual Studio 2022 project, including all source files, the project-local `textures/` folder, and the built executable.
- `Assignment 7-1 Final Project - Design Decisions.docx` — the write-up covering the development choices, the navigation scheme, and the custom functions in the scene.

## The scene

Nine objects sit on the desk: the desktop itself, a monitor, a full-size mechanical keyboard, a mouse and pad, two shelf figurines, an articulated desk lamp, and an insulated tumbler. Everything is assembled from boxes, cylinders, tapered cylinders, spheres, and cones. Four lights carry the room, each one motivated by something actually in it: the lamp bulb, the window, the ceiling fixture, and the glow off the monitor panel. Eight materials cover the surfaces, with the display face and the bulb drawn unlit because no material can describe a surface that makes its own light.

Navigation is WASD to move, Q and E for vertical travel, mouse look for yaw and pitch, and the scroll wheel to change movement speed. A bar down the left edge of the window fills and drains with that speed. P and O switch between perspective and orthographic projection without moving the camera.

**Building it:** the project expects the standard `CS330Content` layout, since the shaders, shape library, and course textures live in a shared `Utilities` folder two levels up. Dropped into that structure it builds and runs in Visual Studio 2022 as-is.

## Reflection

### How do I approach designing software?

The skill this project actually built was decomposition: looking at a real object and deciding which handful of primitives make it read as itself. A mouse is not a shape you can look up. It is a broad palm hump and a long low nose, and it stays a featureless black rock until you add the gray trim that breaks up the silhouette. Learning to make that call, and to make it in real units so the mouse and the pad it sits on are still the right size relative to each other, mattered more than any single technique.

My process was reference first. I started with photos of the desk and a shape breakdown that named every object and the primitives it would take, then built outward one object at a time across the milestones, checking each against the real thing sitting in front of me. Working from a fixed reference meant "done" was something I could decide rather than argue with myself about.

The tactic worth carrying forward is building the cheap version first and then asking whether the expensive one buys anything. Most of the time it does not. The keyboard was the exception: one textured box would have been far cheaper, but the grid of gaps between keycaps is the whole reason a keyboard looks like a keyboard, so I placed all 106 caps from a layout table. Having built the cheap version first is what made that trade-off obvious instead of theoretical.

### How do I approach developing programs?

The main structural strategy was one render helper per object, from `RenderDeskSurface()` through `RenderTumbler()`, which left the main render call a short readable list and meant moving an object was a single edit in a single place. Anything that does not change between frames (e.g., texture loading, material definitions, light setup) got hoisted out of the render loop and run once at startup. Camera movement is scaled by delta time so it covers the same distance per second regardless of frame rate. The speed indicator draws in a screen-space pass that saves and restores every piece of state it touches, which is a pattern I would reuse for any flat readout over a 3D scene.

Iteration drove nearly everything. The desktop began as a flat plane until the monitor base clipped through its underside, so it became a thin slab. A blue backlight under the keys looked great in isolation and took over the whole scene, so I pulled it. My first mouse pad was small enough to make a correctly sized mouse look oversized. Lighting took the most passes, because the course fragment shader gamma corrects its output and sums all four lights before clamping, so every value had to be set far lower than it looked like it should be. I only found that by tuning, failing, and tuning again.

The other thing that changed across the milestones was how much I planned for the code leaving my machine. Halfway through I hit a half-sphere draw call that exists in the mesh library but is never declared in the shared header, and that header lives outside my project folder. I could have fixed it locally in ten seconds, but the fix would not have traveled in the zip and the build would have failed on someone else's machine, so the tumbler emblem became a stack of eight boxes fitted to the arc with the circle equation. Early on I was writing code that ran. By the end I was writing code that runs somewhere else, with comments that still match what the code does.

### How can computer science help me in reaching my goals?

Graphics made me comfortable with things I had only half-understood before: transformation matrices, coordinate spaces, how a projection actually changes what you see, and how much of what looks like art is really arithmetic. Calculating the lamp's joint positions from the arm lengths and angles below it, so that changing one angle swings everything above it, was the moment that clicked. That kind of reasoning carries into anything involving simulation, geometry, or spatial data. Just as useful was working inside a framework I did not write and could not modify, which is closer to real work than any project I start from an empty folder.

I am heading toward cybersecurity, and the connection there is more direct than it first looks. Security work drowns in data that means nothing as a table and a great deal as a picture: traffic over time, attack paths through a network, a login pattern that is obviously wrong the second you plot it. Knowing how a visualization gets built, and what makes one readable instead of merely present, is a real advantage. The habit I picked up here is the one I expect to lean on most, though. I could not test a camera speed control I could not see, so I built a bar on screen to show me. Making the invisible visible is most of debugging, and most of defense.
