export default function Field({ label, error, ...inputProps }) {
    return (
        <label className="field">
            <span>{label}</span>
            <input {...inputProps} aria-invalid={error ? 'true' : undefined} />
            {error && <small className="field-error">{error}</small>}
        </label>
    )
}