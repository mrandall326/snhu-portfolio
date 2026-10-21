# CS 370 — Pirate Intelligent Agent

This project trains a pirate non-player character to find the treasure in a maze using deep Q-learning. The pirate starts out knowing nothing about the maze and learns, over a few hundred games of trial and error, which move to make from every open cell so it can reach the treasure before the human player does.

## Repository Contents

- **[Randall_Matthew_ProjectTwo.ipynb](Randall_Matthew_ProjectTwo.ipynb)** is the completed Jupyter Notebook with the trained agent and its output.
- **[Randall_Matthew_ProjectTwo.html](Randall_Matthew_ProjectTwo.html)** is an HTML export of the same notebook, so the training output and maze images can be viewed without running anything.
- **[Randall_Matthew_ProjectTwo_DesignDefense.docx](Randall_Matthew_ProjectTwo_DesignDefense.docx)** is the design defense that went with the final project, comparing how a person and the agent each solve the maze and evaluating deep Q-learning against search algorithms like A*.
- **[TreasureMaze.py](TreasureMaze.py)**, **[GameExperience.py](GameExperience.py)**, and **[requirements.txt](requirements.txt)** are the provided environment files the notebook needs in order to run. These were given as part of the assignment and were not modified.

## Built With

**Python** · **Jupyter Notebook** · **TensorFlow / Keras** · **NumPy** · **Matplotlib**

## Results

The agent reached a 100% win rate and passed the completion check from every open cell at epoch 297, with training finishing in about six minutes.

## Reflection

### What code was I given, and what code did I create?

The course gave me most of the structure the agent runs in. `TreasureMaze.py` defines the 8x8 maze as a matrix and handles the rules, including which moves are valid and what reward each move earns, and `GameExperience.py` stores past moves in a replay memory and builds the training batches from them. The notebook also came with the neural network already defined, which is two hidden layers with PReLU activations that take in the flattened maze and output a Q-value for each of the four directions. The `play_game` and `completion_check` functions, the setup for a target network, and the epsilon schedule that lowers exploration over time were provided as well.

What I wrote was the Q-training loop itself, which is the part of `qtrain` that actually teaches the agent. Each epoch drops the pirate on a random open cell so it learns the whole maze instead of one route, and then it keeps playing until it wins or loses. On every move it uses an epsilon-greedy choice, taking a random valid move some of the time and the move the network rates highest the rest of the time. Each move gets stored in the replay memory, a random batch is pulled back out to train the network, and the win history and loss get tracked so the progress printout and the stopping condition work. I also added the step that copies the main network's weights into the target network every 50 epochs.

A couple of smaller fixes came up along the way. The lab environment in Codio threw a GPU error as soon as TensorFlow tried to do any math, so I set the notebook to run on the CPU only. I also found that the replay memory checks whether a game is over with a plain true/false test, which meant that storing the game status as a word like "not_over" would have counted every move as the end of a game, so I stored it as a true/false flag instead. Finally, I wrapped the completion check and the test game in print statements so their results would show up in the output instead of being hidden behind the maze image.

### What do computer scientists do, and why does it matter?

From what I've seen in this course and the program as a whole, computer scientists take problems that people deal with every day and figure out how to describe them precisely enough that a computer can help solve them. Sometimes that means writing out every rule by hand, and sometimes, like with the pirate, it means setting up the right rewards and letting the system work out the rules on its own. This course showed me how much of that second kind of work is already shaping daily life, from the recommendations in a social media feed to the way game characters move. It matters because these systems end up making decisions for and about people at a scale no single person could, so how well they're designed affects a lot of lives at once.

### How do I approach a problem as a computer scientist?

I try to start by understanding the problem before I start writing code. With the pirate agent, that meant reading the specifications, working through the starter code to see what each piece already did, and figuring out exactly where my part fit in before I wrote anything. From there I break the problem into smaller steps I can check one at a time, and I test as I go so I know which change caused which result. The game-over flag bug is a good example, because I only caught it by reading how the provided code used the values I was passing in, rather than assuming it would work.

I also try to pick the tool that fits the problem instead of the one that sounds the most impressive. In the design defense I pointed out that a search algorithm like A* would solve a small maze like this one almost instantly, while deep Q-learning took about six minutes. Learning makes more sense when there's no map to search or when the environment keeps changing, and being able to say why one approach fits better than another is a big part of how I want to approach problems going forward.

### What are my ethical responsibilities to the end user and the organization?

My Project One white paper was about a social media company using neural networks for personalization under the GDPR, and writing it made a lot of these responsibilities concrete for me. A model trained only to maximize clicks or time spent can learn to push sensational content, and it can pick up bias from historical data even when it never sees things like race or gender directly. To the end user, I owe honesty about what data is collected and how it's used, collecting only what's actually needed, protecting it properly, and giving people a real way to understand and push back on decisions an automated system makes about them.

To the organization, I owe work that keeps the company within the law and protects its reputation, and that sometimes means raising a concern early even when it's inconvenient, because fixing a privacy or bias problem before launch costs far less than fixing it after a regulator or the public finds it. Even something as small as the pirate agent shows how this works, since the agent does exactly what its rewards encourage and nothing more. Whoever designs those rewards is responsible for what the system ends up doing, and I think that applies to any system I build that affects real people.
