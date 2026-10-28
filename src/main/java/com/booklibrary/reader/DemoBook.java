package com.booklibrary.reader;

/**
 * A short story written for the project, so the reader can be tried without internet.
 * It moves through calm, romantic, action, sad and uplifting parts to show the music changing.
 *
 * Author: Farid Rahmani
 * Contribution: Wrote the demo story used to test the reading mode.
 */
public final class DemoBook {

    public static final String TITLE = "The Lighthouse Letter (demo)";

    private DemoBook() {
    }

    /**
     * Returns the demo story.
     *
     * @return plain text with paragraphs separated by blank lines
     */
    public static String text() {
        return """
                Chapter One: The Quiet Harbour

                The morning came slowly to the little harbour town. A soft breeze moved along the quiet river, and the garden behind the old house was peaceful and still. Clara sat with a cup of tea in the pale sunlight, watching the boats rest at the pier. It was a pleasant, serene morning, the kind that made her feel that nothing in the world could hurry her.

                She walked slowly down the meadow path, past the gentle hills, until the lighthouse stood before her, white against the calm sky. In her pocket was a letter she had carried for many years. She had never found the courage to open it.

                Chapter Two: A Tender Meeting

                At the foot of the lighthouse stood a young man named Thomas, and when he looked at her, her heart began to beat faster. He smiled with such tenderness that she felt herself blush. "I have waited so long to meet you," he whispered. "Every night I have dreamed of this moment, dear Clara."

                They spoke of love and of beauty, of the sweet years that had passed and the devotion that had never faded. He took her hand, and for a moment she felt only affection and a deep, gentle passion. He kissed her hand, and she knew that she loved him.

                Chapter Three: The Storm

                Then the sky turned black. A great storm struck the coast without warning! The waves crashed against the rocks, and the lighthouse shook with the force of the wind. "Run!" shouted Thomas. "Run for the stairs! Attack of the sea is coming, we must escape!"

                They fled up the stone steps as the enemy of wind and water charged against the door. A window smashed, and the sword of lightning struck the tower. Clara screamed, and Thomas fought to hold the door against the roaring storm. Danger was everywhere. They had to hurry, they had to run, quick, quicker! The battle with the sea seemed endless!

                Chapter Four: Silence

                When at last the storm was over, the night was dark and silent. Footsteps echoed on the stairs, and a strange shadow crept across the wall. Clara felt a cold fear and trembled. Someone was hiding in the darkness. Who was it? What did he want? She was afraid to breathe.

                But it was only the old keeper, tired and weeping. He had lost his wife many years before, and he cried with sorrow and loneliness as he spoke of her death. "She wrote this letter," he said, and gave her the old paper with trembling hands. Clara wept with him, for the grief was great and the loss was deep.

                Chapter Five: The Letter

                She opened the letter at last. It said: "Have courage, my dear one. Believe in your dreams, for hope is always stronger than fear. Grow, laugh and be brave. Your life has purpose, and happiness is waiting for you."

                Clara smiled through her tears. Joy rose in her like the morning sun. Thomas took her hand, and together they walked out into the bright new day, full of hope, courage and freedom. Success and happiness were no longer dreams; they were the next step on the road ahead.
                """;
    }
}
