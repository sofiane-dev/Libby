import React from "react";
import validator from "validator";
import RichTextInput from "ra-input-rich-text";
import Grid from "@material-ui/core/Grid";
import {
    AutocompleteInput,
    Create,
    FormDataConsumer,
    NumberInput,
    ReferenceInput,
    required,
    SaveButton,
    SimpleForm,
    TextInput,
    Toolbar,
    translate
} from "react-admin";
import AvatarField from "../../../core/field/AvatarField";
import Typography from "@material-ui/core/Typography";


const isUrlValidation = (value, allValues, props) => {
    if (value && !validator.isURL(value + "", {protocols: ["http", "https"]})) {
        return props.translate("resources.tasksPublic.validation.urlFormat");
    }
    return undefined;
};

const isYearValidation = (value, allValues, props) => {
    if (value && !validator.isInt(value + "", {min: 0, max: 3000, allow_leading_zeroes: false})) {
        return props.translate("resources.tasksPublic.validation.yearFormat");
    }
    return undefined;
};

const isISBN13Validation = (value, allValues, props) => {
    if (value && !validator.isISBN(value + "", "13")) {
        return props.translate("resources.tasksPublic.validation.ISBN13Format");
    }
    return undefined;
};

const validateISBN13 = [isISBN13Validation];
const validateYear = [isYearValidation];
const validateUrl = [isUrlValidation];

const TaskCreateToolbar = props => (
    <Toolbar {...props}>
        <SaveButton
            redirect="edit"
            submitOnEnter={true}
        />
    </Toolbar>
);

const TaskCreate = ({ translate,permissions, ...props}) => (
    <Create {...props}>
        <SimpleForm toolbar={<TaskCreateToolbar/>}
                    defaultValue={{
                        bookOriginalPublicationYear: new Date().getFullYear(),
                        bookSmallImageUrl: "https://picsum.photos/200/300/?random"
                    }}
        >
            <Grid container direction="row" spacing={32} >
                <Grid item xs={12}>
                </Grid>
                <Grid item xs={12}>
                </Grid>
            </Grid>
            <Typography component="h2" variant="h1" color="inherit" align="left">
                {translate("resources.tasksPublic.edit.bookFieldGroup")}
            </Typography>
            <Grid container direction="row" spacing={32} >
                <Grid item>
                    <TextInput  autoFocus source="bookTitle" validate={required()} resettable style={{width: "400px"}} label="resources.tasksPublic.fields.bookTitle"/>
                </Grid>

                <Grid item>
                    <TextInput source="bookAuthors" validate={required()} style={{width: "400px"}} label="resources.tasksPublic.fields.bookAuthors"/>
                </Grid>
            </Grid>

            <Grid container direction="row" spacing={32}>

                <Grid item>
                    <FormDataConsumer>
                        {({ formData, ...rest }) =>
                            <AvatarField size={180} source="bookSmallImageUrl" record={formData} label="resources.tasksPublic.fields.bookSmallImageUrl"/>
                        }
                    </FormDataConsumer>
                </Grid>
                <Grid item>
                    <TextInput source="bookSmallImageUrl" validate={validateUrl} label="resources.tasksPublic.fields.bookSmallImageUrl"/>
                </Grid>
                <Grid item>
                    <RichTextInput source="bookName" label="resources.tasksPublic.fields.bookName"/>
                </Grid>
            </Grid>

            <Grid container direction="row" spacing={32}>
                <Grid item>
                    <NumberInput source="bookOriginalPublicationYear" validate={validateYear} label="resources.tasksPublic.fields.bookOriginalPublicationYear"/>
                </Grid>
                <Grid item>
                    <ReferenceInput source="bookLang.id" reference="langsPublic" label="resources.tasksPublic.fields.bookLang.code">
                        <AutocompleteInput optionText="code" />
                    </ReferenceInput>
                </Grid>
                <Grid item>
                    <TextInput source="bookIsbn" label="resources.tasksPublic.fields.bookIsbn"/>
                </Grid>
                <Grid item>
                    <TextInput source="bookIsbn13" label="resources.tasksPublic.fields.bookIsbn13" validate={validateISBN13}/>
                </Grid>
            </Grid>
        </SimpleForm>
    </Create>
);

export default translate(TaskCreate);