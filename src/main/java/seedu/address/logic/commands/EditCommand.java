package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_HEIGHT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_JERSEY_NUMBER;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PLAYER;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_WEIGHT;
//import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.core.Messages;
//import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lineup.Lineup;
import seedu.address.model.lineup.LineupName;
import seedu.address.model.lineup.LineupPlayersList;
import seedu.address.model.person.Email;
import seedu.address.model.person.Height;
import seedu.address.model.person.JerseyNumber;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Weight;
import seedu.address.model.tag.Tag;

/**
 * Edits the details of an existing person in MyGM.
 */
public class EditCommand extends Command {

    public static final String COMMAND_WORD = "edit";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the person identified "
            + "by the index number used in the displayed person list. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: " + PREFIX_PLAYER + "PERSON_NAME "
            + "[" + PREFIX_NAME + "NAME] "
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_HEIGHT + "HEIGHT] "
            + "[" + PREFIX_WEIGHT + "WEIGHT] "
            + "[" + PREFIX_JERSEY_NUMBER + "JERSEY_NUMBER] "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_PHONE + "91234567 "
            + PREFIX_EMAIL + "johndoe@example.com";

    public static final String MESSAGE_EDIT_PERSON_SUCCESS = "Edited Person: %1$s";
    public static final String MESSAGE_EDIT_LINEUP_SUCCESS = "Edited Lineup: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_DUPLICATE_PERSON = "This person already exists in MyGM.";
    public static final String MESSAGE_DUPLICATE_LINEUP = "This lineup already exists in MyGM.";

    private enum EDIT_COMMAND_TYPE {
        PLAYER, LINEUP
    }

    private final EDIT_COMMAND_TYPE type;
    private final Name targetPlayerName;
    private final EditPersonDescriptor editPersonDescriptor;
    private final LineupName targetLineupName;
    private final LineupName editLineupName;

    /**
     * Constructs an EditCommand for Person
     *
     * @param targetPlayerName     of the person in the filtered person list to edit
     * @param editPersonDescriptor details to edit the person with
     */
    public EditCommand(Name targetPlayerName, EditPersonDescriptor editPersonDescriptor) {
        requireNonNull(targetPlayerName);
        requireNonNull(editPersonDescriptor);

        this.type = EDIT_COMMAND_TYPE.PLAYER;
        this.targetPlayerName = targetPlayerName;
        this.editPersonDescriptor = new EditPersonDescriptor(editPersonDescriptor);
        this.targetLineupName = null;
        this.editLineupName = null;
    }

    /**
     * Constructs an EditCommand for Lineup
     *
     * @param targetLineupName The target LineupName to edit
     * @param editLineupName The new LineupName
     */
    public EditCommand(LineupName targetLineupName, LineupName editLineupName) {
        requireNonNull(targetLineupName);
        requireNonNull(editLineupName);

        this.type = EDIT_COMMAND_TYPE.LINEUP;
        this.targetPlayerName = null;
        this.editPersonDescriptor = null;
        this.targetLineupName = targetLineupName;
        this.editLineupName = editLineupName;
    }

    /**
     * Executes the EditCommand and returns the result message.
     *
     * @param model {@code Model} which the command should operate on.
     * @return feedback message of the operation result for display
     * @throws CommandException If an error occurs during command execution.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (this.type == EDIT_COMMAND_TYPE.PLAYER) {
            if (!model.hasPersonName(targetPlayerName)) { // check if UPL name to person have targetPerson
                throw new CommandException(Messages.MESSAGE_INVALID_PERSON);
            }

            Person personToEdit = model.getPerson(targetPlayerName);
            Person editedPerson = createEditedPerson(personToEdit, editPersonDescriptor);

            if (!personToEdit.isSamePerson(editedPerson) && model.hasPerson(editedPerson)) {
                throw new CommandException(MESSAGE_DUPLICATE_PERSON);
            }

            model.setPerson(personToEdit, editedPerson);
            return new CommandResult(String.format(MESSAGE_EDIT_PERSON_SUCCESS, editedPerson));
        } else {
            if (!model.hasLineupName(targetLineupName)) { // check if UPL name to person have targetPerson
                throw new CommandException(Messages.MESSAGE_INVALID_LINEUP);
            }

            Lineup lineupToEdit = model.getLineup(targetLineupName);
            Lineup editedLineup = createEditedLineup(lineupToEdit, editLineupName);

            if (!targetLineupName.equals(editLineupName) && model.hasLineupName(editLineupName)) {
                throw new CommandException(MESSAGE_DUPLICATE_LINEUP);
            }

            model.setLineup(lineupToEdit, editedLineup);
            return new CommandResult(String.format(MESSAGE_EDIT_LINEUP_SUCCESS, editedLineup));
        }
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code personToEdit}
     * edited with {@code editPersonDescriptor}.
     */
    private static Person createEditedPerson(Person personToEdit, EditPersonDescriptor editPersonDescriptor) {
        assert personToEdit != null;

        Name updatedName = editPersonDescriptor.getName().orElse(personToEdit.getName());
        Phone updatedPhone = editPersonDescriptor.getPhone().orElse(personToEdit.getPhone());
        Email updatedEmail = editPersonDescriptor.getEmail().orElse(personToEdit.getEmail());
        Height updatedHeight = editPersonDescriptor.getHeight().orElse(personToEdit.getHeight());
        Weight updatedWeight = editPersonDescriptor.getWeight().orElse(personToEdit.getWeight());
        JerseyNumber updatedJerseyNumber = editPersonDescriptor.getJerseyNumber()
                .orElse(personToEdit.getJerseyNumber());
        Set<Tag> updatedTags = editPersonDescriptor.getTags().orElse(personToEdit.getTags());
        Set<LineupName> lineupNames = personToEdit.getModifiableLineupNames();

        return new Person(updatedName, updatedPhone, updatedEmail,
                updatedHeight, updatedJerseyNumber, updatedTags, updatedWeight, lineupNames);
    }

    /**
     * Creates and return a {@code Lineup} with the new Lineup name
     */
    private static Lineup createEditedLineup(Lineup lineupToEdit, LineupName editLineupName) {
        assert lineupToEdit != null;

        LineupName updatedName = editLineupName;
        LineupPlayersList playersList = lineupToEdit.getPlayers();
        playersList.replaceLineup(lineupToEdit.getLineupName(), updatedName);

        return new Lineup(updatedName, playersList);
    }

    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditCommand)) {
            return false;
        }

        // state check
        EditCommand e = (EditCommand) other;
        return editPersonDescriptor.equals(e.editPersonDescriptor);
    }

    /**
     * Stores the details to edit the person with. Each non-empty field value will replace the
     * corresponding field value of the person.
     */
    public static class EditPersonDescriptor {
        private Name name;
        private Phone phone;
        private Email email;
        private Height height;
        private JerseyNumber jerseyNumber;
        private Set<Tag> tags;
        private Weight weight;

        public EditPersonDescriptor() {
        }

        /**
         * Copy constructor.
         * A defensive copy of {@code tags} is used internally.
         */
        public EditPersonDescriptor(EditPersonDescriptor toCopy) {
            setName(toCopy.name);
            setPhone(toCopy.phone);
            setEmail(toCopy.email);
            setHeight(toCopy.height);
            setJerseyNumber(toCopy.jerseyNumber);
            setTags(toCopy.tags);
            setWeight(toCopy.weight);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, phone, email, height, jerseyNumber, tags, weight);
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Optional<Name> getName() {
            return Optional.ofNullable(name);
        }

        public void setPhone(Phone phone) {
            this.phone = phone;
        }

        public Optional<Phone> getPhone() {
            return Optional.ofNullable(phone);
        }

        public void setEmail(Email email) {
            this.email = email;
        }

        public Optional<Email> getEmail() {
            return Optional.ofNullable(email);
        }

        public void setHeight(Height height) {
            this.height = height;
        }

        public Optional<Height> getHeight() {
            return Optional.ofNullable(height);
        }

        public void setJerseyNumber(JerseyNumber jerseyNumber) {
            this.jerseyNumber = jerseyNumber;
        }

        public Optional<JerseyNumber> getJerseyNumber() {
            return Optional.ofNullable(jerseyNumber);
        }

        public void setWeight(Weight weight) {
            this.weight = weight;
        }

        public Optional<Weight> getWeight() {
            return Optional.ofNullable(weight);
        }

        /**
         * Sets {@code tags} to this object's {@code tags}.
         * A defensive copy of {@code tags} is used internally.
         */
        public void setTags(Set<Tag> tags) {
            this.tags = (tags != null) ? new HashSet<>(tags) : null;
        }

        /**
         * Returns an unmodifiable tag set, which throws {@code UnsupportedOperationException}
         * if modification is attempted.
         * Returns {@code Optional#empty()} if {@code tags} is null.
         */
        public Optional<Set<Tag>> getTags() {
            return (tags != null) ? Optional.of(Collections.unmodifiableSet(tags)) : Optional.empty();
        }

        @Override
        public boolean equals(Object other) {
            // short circuit if same object
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditPersonDescriptor)) {
                return false;
            }

            // state check
            EditPersonDescriptor e = (EditPersonDescriptor) other;

            return getName().equals(e.getName())
                    && getPhone().equals(e.getPhone())
                    && getEmail().equals(e.getEmail())
                    && getHeight().equals(e.getHeight())
                    && getJerseyNumber().equals(e.getJerseyNumber())
                    && getWeight().equals(e.getWeight())
                    && getTags().equals(e.getTags());
        }
    }
}