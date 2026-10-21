# CS 360 — Scalable, a Weight-Tracking App

Scalable is an Android app I designed and built over the course of CS 360. It lets a person create an account, record one weigh-in a day, set a goal weight, and get a text message when a weigh-in reaches that goal, with everything stored in a SQLite database on the phone.

## Repository Contents

- **[Randall_Matthew_Scalable.zip](Randall_Matthew_Scalable.zip)** is the completed Android Studio project from Project Three. It contains the full Java source, the finalized UI layouts I first designed in Project Two, and the unit tests.
- **[Randall_Matthew_Scalable_Option3_Launch_Plan.docx](Randall_Matthew_Scalable_Option3_Launch_Plan.docx)** is the launch plan covering the store listing, app icon, supported Android versions, permissions, monetization, and how the app would reach its users.

## Built With

**Java** · **Android Studio** · **SQLite** · **Material 3** · **Navigation Component** · **RecyclerView** · **JUnit**

The app runs on Android 14 (API 34) and newer and targets API 36. I tested it on my own Pixel 9 and on the Android Studio emulator.

## Reflection

### What were the requirements and goals of the app, and what user needs did it address?

The assignment called for a weight-tracking app with a login and account creation screen, a database holding users, daily weights, and a goal weight, a grid that displays the user's history, and a text message notification when the goal is reached. The need underneath all of that is pretty simple, because when someone steps off the scale in the morning the number is usually gone from their mind by lunch, and a list of numbers on a calendar can't easily show whether the past month actually went anywhere. I wanted Scalable to be a place where that number would still be there months later and where the whole history would read as a direction instead of a pile of data.

When I planned the app I thought about four kinds of people who would use it, which were someone working down toward a target, a lifter who is gaining weight on purpose, someone in maintenance who just wants to catch a drift, and someone weighing in daily because a doctor told them to. All four want the same record but read it differently, so a big goal for me was keeping the app neutral about which direction counts as progress.

### What screens and features were needed, and how did the UI keep users in mind?

The app ended up with four screens and one dialog, all inside a single activity using the Navigation Component.

- **Login**, which also handles creating an account, and remembers the session so the user isn't asked to sign in every morning.
- **Weight log**, which is the home screen. It has a summary card at the top showing progress toward the goal and a grid of dated weigh-ins below it, newest first, with the change from the previous entry on each row.
- **Add and edit dialog**, which opens over the grid with today's date already filled in, so the usual case is just typing a number and tapping Save.
- **Goal screen**, where the user sets or changes their target and can see which direction the app is going to watch.
- **SMS settings screen**, where text notifications can be turned on, a phone number entered, and the SMS permission requested. Texts are off until the user turns them on.

I kept users in mind mostly by thinking about the moment the app actually gets used, which is usually a few seconds after stepping off a scale and often one-handed. That's why the floating add button is the only floating button in the app, why the dialog stays on top of the grid instead of moving to a new screen, why touch targets are at least 48dp, and why entries can be edited or deleted (by the trash icon or by a long press) so a typo doesn't live in the history forever. I also avoided green-for-loss and red-for-gain coloring, since that would tell the lifter they were doing it wrong. I think the design worked because each screen has one obvious job, and the screen people see most is the record itself rather than a dashboard they have to dig through.

### How did I approach coding the app, and what techniques could I use again?

I built the app in stages that followed the course projects. Project Two was the interface with sample data, so I could see the screens working and get the layout right before anything was saved for real. In Project Three I replaced the sample data with a SQLite database and wrote a separate DAO class for each table (`UserDao`, `WeightDao`, and `GoalDao`), so the screens never write SQL themselves and only ask the DAOs for what they need. Passwords are stored as salted PBKDF2 hashes through a `PasswordHasher` class, and the progress and direction logic lives in a plain Java `GoalWeight` class that doesn't depend on Android at all.

The technique I'd carry forward most is keeping logic out of the UI classes. Because the goal math and the password hashing were plain Java, I could test them on their own, and if I ever change how a screen looks, the rules behind it stay the same. Building the UI first with fake data and then swapping in the real data layer is also something I'd do again, since it let me catch layout problems early without worrying about the database at the same time.

### How did I test the code, and what did it reveal?

I used two kinds of testing. The first was JUnit unit tests for the classes that don't need a phone to run, covering password hashing (correct and wrong passwords, unique salts, and damaged stored values), the goal progress math (both directions, and progress that never goes negative or shows 100% early), and phone number validation for the SMS screen. The second was running the app by hand on both my Pixel 9 and the emulator, going through account creation, adding, editing, and deleting entries, setting goals in both directions, and testing both the Allow and Don't Allow paths of the SMS permission.

Testing on a real phone turned out to matter a lot, because a few problems never showed up on the emulator. On my Pixel 9 the status bar was taking extra padding at the top of the screen, the keyboard covered the Save button on the goal screen, and the logo on the login screen was getting clipped. Real use also exposed a logic bug where a weight-loss goal could be marked as reached too early, which I fixed by locking in the goal's direction at the moment it's saved instead of working it out again later. SMS was another difference between the two, since texts are awkward to test on the emulator, and on my phone I found that a text sent to my own number arrived but didn't pop up a notification, which is just how Google Messages handles messages to yourself. That taught me that a message being sent isn't the same as it being seen, and it's part of why the launch plan calls for a regular notification in a store version.

### Where did I have to innovate to overcome a challenge?

The biggest challenge was making the goal feature work for people headed in either direction. My first approach worked out the direction by comparing the goal to the oldest weigh-in, but that broke as soon as someone's history didn't start where their current goal did. I changed it so the app records the starting weight and the direction when the goal is saved, and then I reworked the progress bar so it shows the start, the goal, and a percentage, and says clearly whether the user has reached the goal or passed it by a certain amount.

Another challenge came up while writing the launch plan, when I found that Google Play only lets default texting apps use the SEND_SMS permission. The course version keeps the text message because the assignment asks for it, but I planned the store version around a standard notification instead, so the app could actually be published.

### What component best shows my knowledge and skills?

I think the data layer is where I showed the most. Splitting the database into three tables with foreign keys and a unique constraint on one weigh-in per user per day, giving each table its own DAO, and hashing passwords properly instead of storing them as plain text all come from things I learned in earlier courses, especially CS 305 and CS 340, and it was satisfying to see them come together in a working mobile app. It also made the rest of the work easier, because once the data layer was solid, changes to the screens didn't risk breaking anyone's history.
