# CS 340 — Grazioso Salvare Dashboard

An interactive web dashboard that lets Grazioso Salvare identify dogs in the Austin Animal Center outcomes data that match their search-and-rescue training profiles.

## About the Project

Global Rain was contracted to build a full stack application for Grazioso Salvare, an international rescue-animal training company that identifies dogs suited to search-and-rescue work. The client receives outcome data from a nonprofit operating five animal shelters around Austin, Texas, and needs a way to search it for candidate dogs.

The first phase is a portable, object-oriented CRUD module giving programmatic access to the Austin Animal Center data in MongoDB. The `AnimalShelter` class connects to the `aac` database and `animals` collection and implements all four CRUD operations.

The second phase is the client-facing dashboard built on top of that module. It reads through the same class, filters down to the dogs matching a given rescue profile, and presents the results as an interactive data table, a breed pie chart, and a geolocation map. No part of the interface queries MongoDB directly.

## Motivation

Grazioso Salvare looks for specific profiles when selecting dogs to train: search-and-rescue work is generally more effective on dogs no more than two years old, and different breeds are proficient at different specialties. Each profile is a query against breed, sex, and age.

Identifying a candidate by hand means scanning roughly ten thousand shelter records for a narrow combination of those three fields. The dashboard turns that search into a single click, and centralizing all database interaction in one importable module means either layer can change independently of the other.

## Dashboard Functionality

- **Branding** — the Grazioso Salvare logo wrapped in an anchor tag, and a unique identifier crediting the developer.
- **Interactive filter options** — a radio-button group offering Water Rescue, Mountain or Wilderness Rescue, Disaster or Individual Tracking, and Reset. Each option runs its own query against MongoDB through the CRUD module rather than filtering a cached copy in Python.
- **Interactive data table** — paginated ten rows at a time, sortable and filterable on every column, with single-row selection. Rebuilds from the query results each time the filter changes.
- **Geolocation chart** — a Leaflet map centered on Austin, dropping a marker on the selected animal with breed on the tooltip and name in the popup.
- **Breed chart** — a pie chart of the breeds in the current table view. The ten most common are labelled individually and the remainder grouped into a single "Other breeds" slice.
- **Record count** — a line beneath the filter reporting the rescue type and the number of records returned.

| Rescue Type | Preferred Sex | Training Age | Records |
|---|---|---|---|
| Water | Intact Female | 26–156 weeks | 23 |
| Mountain or Wilderness | Intact Male | 26–156 weeks | 18 |
| Disaster or Individual Tracking | Intact Male | 20–300 weeks | 30 |
| Reset | — | — | 10,000 |

## Built With

**MongoDB** · **PyMongo** · **Python 3** · **JupyterLab** · **Dash** · **JupyterDash** · **Dash Leaflet** · **Plotly Express** · **pandas**

## Repository Contents

| File | Description |
|------|-------------|
| `CRUD_Python_Module.py` | The `AnimalShelter` class — create, read, update, and delete operations against MongoDB |
| `ProjectTwoDashboard.ipynb` | The dashboard application |
| `CS_340_Project_Two_README.docx` | Full project documentation with development notes and screenshots |

## Getting Started

1. Confirm the data set is present:
   ```
   ls datasets/
   ```
2. Import it with `mongoimport`:
   ```
   mongoimport --type=csv --headerline --db=aac \
     --collection=animals ./datasets/aac_shelter_outcomes.csv
   ```
3. Confirm the output reports 10,000 documents imported and 0 failures.
4. Open the shell and switch to the admin authentication database:
   ```
   mongosh
   use admin
   ```
5. Create the application account. Using `passwordPrompt()` keeps the password out of shell history:
   ```
   db.createUser({
     user: "aacuser",
     pwd: passwordPrompt(),
     roles: [{ role: "readWrite", db: "aac" }]
   })
   ```
6. Exit and log back in as the new account to verify it:
   ```
   exit
   mongosh --username "aacuser" --authenticationDatabase admin
   ```
7. Confirm the role with `db.runCommand({connectionStatus: 1})`.
8. Place `CRUD_Python_Module.py` in the same directory as the notebook that will use it.
9. Install the dashboard dependencies:
   ```
   pip install dash jupyter-dash dash-leaflet plotly pandas
   ```
10. Place `ProjectTwoDashboard.ipynb` and the Grazioso Salvare logo PNG in that same directory.
11. Open the notebook and set the `username` and `password` variables in the model section to the credentials created in step 5.
12. Select Kernel, then Restart Kernel and Run All Cells.
13. Open the proxied URL printed by `app.run_server()`. If port 8050 is in use, pass an alternate port.

## Screenshots

![Unfiltered dashboard](screenshots/dashboard-unfiltered.png)

*The dashboard in its starting state — all 10,000 records, with the data table, breed pie chart, and geolocation map.*

![Water Rescue filter](screenshots/dashboard-water-rescue.png)

*Water Rescue — 23 records, all intact females between 26 and 156 weeks.*

![Mountain or Wilderness Rescue filter](screenshots/dashboard-mountain-rescue.png)

*Mountain or Wilderness Rescue — 18 intact males. The pie legend shows the anchored breed pattern matching German Shepherd variants and mixes.*

![Disaster or Individual Tracking filter](screenshots/dashboard-disaster-rescue.png)

*Disaster or Individual Tracking — 30 records across the widest age range, 20 to 300 weeks.*

![Reset](screenshots/dashboard-reset.png)

*Reset from a filtered state — every widget returns to its original condition and the count returns to 10,000.*

---

# Reflection

## How do you write programs that are maintainable, readable, and adaptable?

The clearest example from this course is the CRUD module I wrote in Project One and then reused in Project Two without changing a line of it. Building it as a standalone class meant the dashboard only ever calls `read()`, `create()`, `update()`, and `delete()`. Everything about how the connection gets made stays inside one file: the host, the port, the URI construction, the `authSource=admin` requirement. When I sat down to build the dashboard I was thinking about callbacks and layout, not connection strings.

A few decisions inside that module made it easier to build on.

`read()` returns a list instead of the cursor PyMongo hands back. A cursor is lazily evaluated and gets consumed the first time you iterate it, so a second pass returns nothing at all. No error, no warning, the data just isn't there. That is a miserable thing to debug from inside a Dash callback, so I wrap the cursor in `list()` and hand back something the caller can use more than once.

`update()` and `delete()` return the count of documents affected rather than a boolean. "It worked" and "it worked and changed three records" are different answers, and the caller usually needs the second one. `update()` returns `modified_count` and not `matched_count`, since a document that already holds the value you're writing gets matched without actually changing.

`update()` also takes its update argument either way, as a MongoDB operator like `{'$set': {...}}` or as plain key/value pairs that it wraps in `$set` itself. I went back and forth on that one. Accepting the operator form won out because otherwise `$inc` and `$unset` would need a second method, and two methods that do almost the same thing is how modules start rotting.

Smaller things: the module catches `PyMongoError` instead of a bare `Exception`, so a mistake in my own calling code doesn't get reported as a database failure. It logs instead of printing, which lets whoever imports it decide where messages go, a notebook cell in Project One and a server log in Project Two. Credentials come in as constructor arguments, and the password gets percent-encoded before it goes into the URI, because `@`, `:`, and `/` all mean something inside a connection string.

Adaptability showed up somewhere I wasn't expecting. The starter dashboard code grabbed latitude, longitude, breed, and name by numeric column position. That works right up until the column order changes, and it broke the moment I normalized the order for the data table. Switching to lookups by column name killed the dependency entirely. Positional access is the kind of shortcut that works fine until something upstream moves, which is exactly the situation maintainable code is supposed to survive.

I could point this module at anything backed by the same database. A command-line reporting tool, a scheduled job that flags animals held past a certain date, an intake form that writes new records. Since credentials are passed in rather than baked into the file, a read-only reporting script and a write-capable intake tool can share one implementation and just connect at different permission levels.

## How do you approach a problem as a computer scientist?

I started with the data instead of the interface on this one, which is backwards from how I have handled most assignments. In CS 255 and CS 300 I generally started from the deliverable, the diagram or the data structure the assignment asked for, and worked toward it. That works when the input is clean or made up for the assignment. Here the input was ten thousand real shelter records, and it turned out the requirements document and the actual data did not fully agree.

Before writing any dashboard code I queried the collection in the shell just to see what was in it. How breeds were spelled, how sex and age were recorded, what the outcome types looked like.

That's what saved the project, honestly. Matching the breed field against the exact strings from the requirements table returned almost nothing, because the shelter records most dogs as mixes and crosses. An anchored, case-insensitive regular expression fixed it. The pattern matches any breed that begins with one of the preferred names, so it picks up "German Shepherd Mix" and "German Shepherd/Labrador Retriever" while still leaving out unrelated breeds like "Australian Shepherd Mix" that an unanchored pattern would drag in. Looking through the distinct breed values also turned up that Doberman Pinscher is stored as "Doberman Pinsch." A query written faithfully from the requirements document would have returned zero results for that breed and given no sign anything was wrong.

The other shift was treating the client's requirements as the specification rather than treating the rubric as the specification. If a breed pattern is slightly off, a trainer never sees a suitable dog and has no way to know they missed one. So I checked each filter against the data set directly instead of just confirming the table populated with something. Water Rescue returns 23 records, Mountain or Wilderness 18, Disaster or Individual Tracking 30. Getting those same counts from an independent check is what makes the queries correct instead of merely plausible.

The same thinking shaped how I tested the CRUD module. Every document the test script inserts carries a `test_record` marker, and the update and delete cells filter on it. Both methods act on all matches, so that marker is the only thing standing between a test run and the imported shelter data. I insert two documents rather than one so the returned counts actually show both methods hitting every match instead of just the first.

The decision I'd defend hardest is pushing the filtering into MongoDB instead of pulling the collection into pandas and narrowing it there. Each rescue type is a query document, the database returns only what matches, and the table, pie chart, and map all draw from the same server-side result. Filtering in Python would have been easier to write and honestly would have run fine at ten thousand records. It also throws away the database's indexing and query planning, and it stops working the moment the collection gets big.

For future client work I would keep the same order. Look at the data first, define the interface between the layers before building either side, push data operations down to the database. I would also write down the data-access layer's return types before implementing anything, since the return-value decisions I made back in Project One ended up shaping everything built on top of them.

## What do computer scientists do, and why does it matter?

Computer scientists take problems people are currently solving slowly, or inconsistently, or not at all, and build something that solves them the same way every time. The code is a means to that. The value is in what becomes possible once it exists.

This project is a small version of that. The Austin Animal Center data is public and already has everything Grazioso Salvare needs in it. What it doesn't have is any way for a trainer to ask it a question. Without the dashboard, finding candidate dogs means either scanning ten thousand records by hand or learning MongoDB query syntax, and neither is a reasonable thing to ask of someone whose job is training animals. The dashboard turns it into picking a radio button. The map adds something the raw records don't readily give up, which is where the candidates actually are.

It also makes the search more consistent than a person doing it by hand. The breed matching is one pattern applied identically every time instead of whatever spellings a given searcher happens to think of on a given day. The mixed-breed problem is the clearest case. Someone scanning for "German Shepherd" by eye would probably skip right past "German Shepherd/Labrador Retriever," and a dog that qualified would go unnoticed.

So the practical result is more time spent training animals, less spent on data handling, and fewer suitable dogs missed. That's the general shape of this kind of work: closing the gap between what an organization already knows and what it can actually act on.

## Resources

- [MongoDB documentation](https://www.mongodb.com/docs/)
- [PyMongo documentation](https://pymongo.readthedocs.io/en/stable/)
- [Dash documentation](https://dash.plotly.com/)
- [Dash DataTable reference](https://dash.plotly.com/datatable)
- [Dash Leaflet documentation](https://www.dash-leaflet.com/)
- [Plotly Express documentation](https://plotly.com/python/plotly-express/)
- [pandas documentation](https://pandas.pydata.org/docs/)
- [Austin Animal Center Outcomes data set](https://doi.org/10.26000/025.000001)

## Contact

Matthew Randall
Southern New Hampshire University | CS 340: Client/Server Development
